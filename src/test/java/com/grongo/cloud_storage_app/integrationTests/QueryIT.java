package com.grongo.cloud_storage_app.integrationTests;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.ItemDto;
import com.grongo.cloud_storage_app.models.sharedItems.SharedItem;
import com.grongo.cloud_storage_app.models.tag.Tag;
import com.grongo.cloud_storage_app.models.tag.TagJoin;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.*;
import com.grongo.cloud_storage_app.services.auth.JwtService;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import static com.grongo.cloud_storage_app.TestUtils.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class QueryIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StorageService storageService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private JoinTagRepository joinTagRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private SharedItemRepository sharedItemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl = "/api/items/query";

    private String accessToken;
    private User currentAuthenticatedUser;


    static RedisContainer redisContainer;

    @BeforeAll
    public static void setRedisContainer(){
        redisContainer = new RedisContainer(
                RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG)
        ).withExposedPorts(6379);
        redisContainer.start();

        System.setProperty("spring.redis.host", redisContainer.getHost());
        System.setProperty("spring.redis.port", redisContainer.getMappedPort(6379).toString());

    }

    @BeforeEach
    public void authenticate(){
        currentAuthenticatedUser = User.builder()
                .email("test")
                .username("test")
                .password("test")
                .build();

        userRepository.save(currentAuthenticatedUser);

        accessToken = jwtService.createAccessToken(
                currentAuthenticatedUser.getId(),
                currentAuthenticatedUser.getEmail()
                )
                .getAccessToken();
    }

    private final TypeReference<List<ItemDto>> typeRef = new TypeReference<List<ItemDto>>() {};

    @Test
    public void testIfSizeFilterIsWorking() throws Exception {
        File file1 = getFile("file1", null, currentAuthenticatedUser);
        file1.setSize(1000L);
        fileRepository.save(file1);

        File file2 = getFile("file2", null, currentAuthenticatedUser);
        file2.setSize(500L);
        fileRepository.save(file2);

        //check for file1
        MvcResult mvcRequest1 =  mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "?minBytes=700")
                .header("Authorization", "Bearer " + accessToken)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        List<ItemDto> resultList1 = objectMapper.readValue(mvcRequest1.getResponse().getContentAsString(), typeRef);
        assertThat(resultList1).hasSize(1);
        assertThat(resultList1.getFirst().getId()).isEqualTo(file1.getId());

        //check for file2
        MvcResult mvcRequest2 =  mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "?maxBytes=700")
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        List<ItemDto> resultList2 = objectMapper.readValue(mvcRequest2.getResponse().getContentAsString(), typeRef);
        assertThat(resultList2).hasSize(1);
        assertThat(resultList2.getFirst().getId()).isEqualTo(file2.getId());
    }

    @Test
    public void testIfDateCreationFilterIsWorking() throws Exception {
        File file1 = getFile("file1", null, currentAuthenticatedUser);
        fileRepository.save(file1);

        Thread.sleep(1000);

        Date date = new Date();

        Thread.sleep(1000);

        String isoString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
        File file2 = getFile("file2", null, currentAuthenticatedUser);
        fileRepository.save(file2);

        //  Test file1
        MvcResult result1 = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "?maxDate=" + isoString)
                .header("Authorization", "Bearer " + accessToken)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        List<ItemDto> list1 = objectMapper.readValue(result1.getResponse().getContentAsString(), typeRef);
        assertThat(list1).hasSize(1);
        assertThat(list1.getFirst().getId()).isEqualTo(file1.getId());

        //  Test file2
        MvcResult result2 = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "?minDate=" + isoString)
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andReturn();

        List<ItemDto> list2 = objectMapper.readValue(result2.getResponse().getContentAsString(), typeRef);
        assertThat(list2).hasSize(1);
        assertThat(list2.getFirst().getId()).isEqualTo(file2.getId());
    }

    @Test
    public void testIfTypeFilterIsWorking() throws Exception {
        File file = getFile("file", null, currentAuthenticatedUser);
        fileRepository.save(file);
        Folder folder = getFolder("folder", null, currentAuthenticatedUser);
        folderRepository.save(folder);

        //  Handle file query
        MvcResult fileMvcResult = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "?type=FILE")
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andReturn();

        List<ItemDto> fileList = objectMapper.readValue(fileMvcResult.getResponse().getContentAsString(), typeRef);
        assertThat(fileList.size()).isEqualTo(1);
        assertThat(fileList.getFirst().getId()).isEqualTo(file.getId());


        //  Handle folder query
        MvcResult folderResult = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "?type=FOLDER")
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andReturn();

        List<ItemDto> folderList = objectMapper.readValue(folderResult.getResponse().getContentAsString(), typeRef);
        assertThat(folderList.size()).isEqualTo(1);
        assertThat(folderList.getFirst().getId()).isEqualTo(folder.getId());
    }

    @Test
    public void testIfFileTypeQueryIsWorking() throws Exception {
        File gifFile = getFile("gifFile", null, currentAuthenticatedUser);
        gifFile.setFileType(MediaType.IMAGE_GIF_VALUE);
        gifFile.setType("FILE");
        fileRepository.save(gifFile);

        File pdfFile = getFile("pdfFile", null, currentAuthenticatedUser);
        pdfFile.setType("FILE");
        pdfFile.setFileType(MediaType.APPLICATION_PDF_VALUE);
        fileRepository.save(pdfFile);

        //  QUERY GIF FILE
        MvcResult gifResult = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "?fileType=" + MediaType.IMAGE_GIF_VALUE)
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andReturn();

        List<ItemDto> gifFileList = objectMapper.readValue(gifResult.getResponse().getContentAsString(), typeRef);
        assertThat(gifFileList.size()).isEqualTo(1);
        assertThat(gifFileList.getFirst().getId()).isEqualTo(gifFile.getId());


        //  QUERY PDF FILE
        MvcResult pdfResult = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "?fileType=" + MediaType.APPLICATION_PDF_VALUE)
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andReturn();

        List<ItemDto> pdfFileList = objectMapper.readValue(pdfResult.getResponse().getContentAsString(), typeRef);
        assertThat(pdfFileList.size()).isEqualTo(1);
        assertThat(pdfFileList.getFirst().getId()).isEqualTo(pdfFile.getId());


        //  QUERY BOTH FILES
        MvcResult bothFilesResult = mockMvc.perform(MockMvcRequestBuilders
                .get(baseUrl + "?fileType=" + MediaType.APPLICATION_PDF_VALUE + "&fileType=" + MediaType.IMAGE_GIF_VALUE)
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andReturn();

        List<ItemDto> bothFilesList = objectMapper.readValue(bothFilesResult.getResponse().getContentAsString(), typeRef);
        assertThat(bothFilesList.size()).isEqualTo(2);

        boolean areAllIdsMatching = bothFilesList
                .stream()
                .allMatch(itemDto ->
                        itemDto.getId().equals(gifFile.getId()) || itemDto.getId().equals(pdfFile.getId()
                        )
                );

        assertThat(areAllIdsMatching).isTrue();
    }

    @Test
    public void testIfNameFiltersAreWorking() throws Exception {
        File file1 = getFile("file1", null, currentAuthenticatedUser);
        fileRepository.save(file1);

        File file2 = getFile("file2", null, currentAuthenticatedUser);
        fileRepository.save(file2);

        //  QUERY FILE1 BY NAME
        MvcResult file1Result = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "?name=" + file1.getName())
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andReturn();

        List<ItemDto> file1List = objectMapper.readValue(file1Result.getResponse().getContentAsString(), typeRef);
        assertThat(file1List.size()).isEqualTo(1);
        assertThat(file1List.getFirst().getId()).isEqualTo(file1.getId());
    }

    @Test
    public void testIfTagFilterIsWorking() throws Exception {
        //  CREATING FILE1 WITH TAG1
        File file1 = getFile("file1", null, currentAuthenticatedUser);
        fileRepository.save(file1);

        Tag tag1 = Tag.builder().name("tag1").hex_color("123456").build();
        tagRepository.save(tag1);

        TagJoin tagJoin1 = TagJoin.builder().item(file1).tag(tag1).build();
        joinTagRepository.save(tagJoin1);


        //  CREATING FIL2 WITH TAG2
        File file2 = getFile("file2", null, currentAuthenticatedUser);
        fileRepository.save(file2);

        Tag tag2 = Tag.builder().name("tag2").hex_color("123456").build();
        tagRepository.save(tag2);

        TagJoin tagJoin2 = TagJoin.builder().item(file2).tag(tag2).build();
        joinTagRepository.save(tagJoin2);


        //  QUERY FILE1 BY TAG1
        MvcResult file1Result = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "?tagId=" + tag1.getId())
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andReturn();

        List<ItemDto> file1List = objectMapper.readValue(file1Result.getResponse().getContentAsString(), typeRef);
        assertThat(file1List).hasSize(1);
        assertThat(file1List.getFirst().getId()).isEqualTo(file1.getId());


        //  QUERY FILE 2 BY TAG2
        MvcResult file2Result = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "?tagId=" + tag2.getId())
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andReturn();

        List<ItemDto> file2List = objectMapper.readValue(file2Result.getResponse().getContentAsString(), typeRef);
        assertThat(file2List).hasSize(1);
        assertThat(file2List.getFirst().getId()).isEqualTo(file2.getId());


        //  QUERY BOTH FILES BY TAG1 AND TAG2
        MvcResult bothFilesResult = mockMvc.perform(MockMvcRequestBuilders
                .get(baseUrl + "?tagId=" + tag1.getId() + "&tagId=" + tag2.getId())
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andReturn();

        List<ItemDto> bothItemsList = objectMapper.readValue(bothFilesResult.getResponse().getContentAsString(), typeRef);
        assertThat(bothItemsList).hasSize(2);

        boolean areAllIdsMatching = bothItemsList.stream().allMatch(itemDto ->
                itemDto.getId().equals(file1.getId()) || itemDto.getId().equals(file2.getId())
        );

        assertThat(areAllIdsMatching).isTrue();
    }



    @Test
    public void testIfSharedItemsCanAlsoBeQueried() throws Exception {
        //  CREATE SECONDARY USER
        User resourceOwner = User.builder().username("test1").email("test1").build();
        userRepository.save(resourceOwner);

        File sharedFile = getFile("sharedFile", null, resourceOwner);
        fileRepository.save(sharedFile);

        SharedItem sharedItem = SharedItem.builder()
                .owner(resourceOwner)
                .user(currentAuthenticatedUser)
                .item(sharedFile)
                .build();

        sharedItemRepository.save(sharedItem);

        MvcResult sharedItemResult = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl)
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andReturn();


        List<ItemDto> sharedItemList = objectMapper.readValue(sharedItemResult.getResponse().getContentAsString(), typeRef);
        assertThat(sharedItemList).hasSize(1);
        assertThat(sharedItemList.getFirst().getId()).isEqualTo(sharedFile.getId());
    }

}
