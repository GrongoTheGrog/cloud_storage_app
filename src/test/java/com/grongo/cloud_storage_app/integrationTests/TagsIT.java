package com.grongo.cloud_storage_app.integrationTests;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.tag.Tag;
import com.grongo.cloud_storage_app.models.tag.TagJoin;
import com.grongo.cloud_storage_app.models.tag.dtos.TagCreationDto;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.JoinTagRepository;
import com.grongo.cloud_storage_app.repositories.TagRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.jwt.JwtAccessService;
import com.grongo.cloud_storage_app.services.jwt.JwtRefreshService;
import com.grongo.cloud_storage_app.services.tag.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static com.grongo.cloud_storage_app.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TagsIT {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtRefreshService jwtRefreshService;
    @Autowired
    private JwtAccessService jwtAccessService;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JoinTagRepository joinTagRepository;
    @Autowired
    private TagService tagService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private User currentAuthenticatedUser;
    private String accessToken;

    @BeforeEach
    public void authenticate(){
        currentAuthenticatedUser = User.builder()
                .email("test")
                .username("test")
                .password("test")
                .build();

        userRepository.save(currentAuthenticatedUser);
        accessToken = jwtAccessService.create(
                        currentAuthenticatedUser.getId(),
                        currentAuthenticatedUser.getEmail()
        );
    }


    @Test
    public void testIfTagsCanBeCreated() throws Exception {
        TagCreationDto tagCreationDto = TagCreationDto.builder()
                .name("test")
                .hex_color("243214")
                .description("test")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tags")
                .contentType("application/json")
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken)
                .content(objectMapper.writeValueAsString(tagCreationDto))
        ).andExpect(status().isCreated());

        List<Tag> tagList = tagRepository.findAll();
        assertThat(tagList.size()).isEqualTo(1);
        assertThat(tagList.getFirst().getName()).isEqualTo("test");
    }

    @Test
    public void testIfTagsCanBeDeleted() throws Exception {
        Tag tag = Tag.builder().name("name").user(currentAuthenticatedUser).build();
        tagRepository.save(tag);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/tags/" + tag.getId())
                .header("Authorization", "Bearer " + accessToken)
                .with(csrf())
        ).andExpect(status().isNoContent());

        List<Tag> tagList = tagRepository.findAll();
        assertThat(tagList.size()).isEqualTo(0);
    }

    @Test
    public void testIfTagCanBeBoundToAnItem() throws Exception {
        Tag tag = Tag.builder().name("name").user(currentAuthenticatedUser).build();
        tagRepository.save(tag);
        Folder folder = getFolder("test", null, currentAuthenticatedUser);
        folderRepository.save(folder);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/items/" + folder.getId() + "/tag/" + tag.getId())
                .header("Authorization", "Bearer " + accessToken)
                .with(csrf())
        ).andExpect(status().isCreated());


        List<TagJoin> tagJoinList = joinTagRepository.findAll();
        assertThat(tagJoinList.size()).isEqualTo(1);
        assertThat(tagJoinList.getFirst().getTag().getId()).isEqualTo(tag.getId());
        assertThat(tagJoinList.getFirst().getItem().getId()).isEqualTo(folder.getId());
    }

    @Test
    public void testIfTagCanBeUnboundFromAnItem() throws Exception {
        Tag tag = Tag.builder().name("name").user(currentAuthenticatedUser).build();
        tagRepository.save(tag);
        Folder folder = getFolder("test", null, currentAuthenticatedUser);
        folderRepository.save(folder);

        tagService.bindTagToFile(tag.getId(), folder.getId());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/items/" + folder.getId() + "/tag/" + tag.getId())
                .header("Authorization", "Bearer " + accessToken)
                .with(csrf())
        ).andExpect(status().isNoContent());


        List<TagJoin> tagJoinList = joinTagRepository.findAll();
        assertThat(tagJoinList.size()).isEqualTo(0);
    }
}
