package com.grongo.cloud_storage_app.integrationTests;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.dto.FolderDto;
import com.grongo.cloud_storage_app.models.items.dto.FolderRequest;
import com.grongo.cloud_storage_app.models.items.dto.MoveItemRequest;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FileRepository;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.auth.JwtService;
import com.grongo.cloud_storage_app.services.items.FolderService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.grongo.cloud_storage_app.testUtils.TestUtils.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StorageIT {

    @MockitoSpyBean
    S3Client s3Client;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthService authService;

    @Autowired
    JwtService jwtService;

    @Autowired
    FolderRepository folderRepository;

    @Autowired
    FolderService folderService;

    private String accessToken;
    private User currentAuthenticatedUser;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    //GET AN ACCESS TOKEN TO BE ABLE TO PERFORM REQUESTS
    public void authenticate(){
        User user = User.builder()
                .username("test")
                .email("test")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        userRepository.save(user);
        currentAuthenticatedUser = user;

        accessToken = jwtService.createAccessToken(user.getId(), "test").getAccessToken();
    }

    @Nested
    class FileTests {

        @Autowired
        FileRepository fileRepository;

        @Test
        public void testIfFileCanBeCreated() throws Exception {
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    "test.txt",
                    MediaType.TEXT_PLAIN_VALUE,
                    "test".getBytes()
            );

            S3Client mockS3 = mock(S3Client.class);
            PutObjectResponse dummyResponse = mock(PutObjectResponse.class);

            when(mockS3.putObject((PutObjectRequest) any(), (Path) any())).thenReturn(dummyResponse);


            mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files")
                            .file(multipartFile)
                            .header("Authorization", "Bearer " + accessToken)

                    )
                    .andExpect(status().isCreated());

            verify(s3Client).putObject((PutObjectRequest) any(), (Path) any());
            List<File> fileList = fileRepository.findAll();
            assertThat(fileList).hasSize(1);

        }


        @Test
        public void shouldMoveFileToRootIfNullIsProvided() throws Exception {
            Folder folder = getFolder("folder", null, currentAuthenticatedUser);
            File file = getFile("file", folder, currentAuthenticatedUser);

            folderRepository.save(folder);
            fileRepository.save(file);

            assertThat(file.getFolder().getId()).isEqualTo(folder.getId());

            MoveItemRequest moveItemRequest = new MoveItemRequest(null);
            String moveItemRequestJson = objectMapper.writeValueAsString(moveItemRequest);

            mockMvc.perform(MockMvcRequestBuilders.patch("/api/items/move/" + file.getId())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(moveItemRequestJson)
            ).andExpect(status().isNoContent());

            Optional<File> movedFile = fileRepository.findById(file.getId());
            assertThat(movedFile).isPresent();
            assertThat(movedFile.get().getFolder()).isNull();
        }

        @Test
        public void shouldFileMoveToFolderIfIdIsProvided() throws Exception {
            Folder folder = getFolder("folder", null, currentAuthenticatedUser);
            File file = getFile("file", null, currentAuthenticatedUser);

            folderRepository.save(folder);
            fileRepository.save(file);

            assertThat(file.getFolder()).isNull();

            MoveItemRequest moveItemRequest = new MoveItemRequest(folder.getId());
            String moveItemRequestJson = objectMapper.writeValueAsString(moveItemRequest);

            mockMvc.perform(MockMvcRequestBuilders.patch("/api/items/move/" + file.getId())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(moveItemRequestJson)
            ).andExpect(status().isNoContent());

            Optional<File> movedFile = fileRepository.findById(file.getId());
            assertThat(movedFile).isPresent();
            assertThat(movedFile.get().getFolder().getId()).isEqualTo(folder.getId());
        }
    }


    @Nested
    class FolderTests{

        @Test
        public void testIfFolderCanBeCreated() throws Exception {
            FolderRequest folderRequest = new FolderRequest("testFolder", null);
            String folderRequestJson = objectMapper.writeValueAsString(folderRequest);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/folders")
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(folderRequestJson)
            ).andExpect(status().isCreated());

            List<Folder> folderList = folderRepository.findAll();

            assertThat(folderList).hasSize(1);
        }

        @Test
        public void shouldMoveFolderToRootWhenNewFolderIdIsNull() throws Exception {
            Folder folder = getFolder("folder", null, currentAuthenticatedUser);
            Folder folder1 = getFolder("folder1", folder, currentAuthenticatedUser);
            Folder folder2 = getFolder("folder2", folder, currentAuthenticatedUser);

            folderRepository.save(folder);
            folderRepository.save(folder1);
            folderRepository.save(folder2);

            MoveItemRequest moveItemRequest = new MoveItemRequest(null);
            String moveItemRequestJson = objectMapper.writeValueAsString(moveItemRequest);

            mockMvc.perform(MockMvcRequestBuilders.patch("/api/items/move/" + folder1.getId())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(moveItemRequestJson)
            ).andExpect(status().isNoContent());

            FolderDto movedFolder = folderService.findFolderById(folder1.getId());

            FolderDto parentFolderDto = movedFolder.getFolder();
            assertThat(parentFolderDto).isNull();
        }

        @Test
        public void shouldMoveFolderToFolderIfIdIsProvided() throws Exception {
            Folder folder = getFolder("folder", null, currentAuthenticatedUser);
            Folder folder1 = getFolder("folder1", folder, currentAuthenticatedUser);
            Folder folder2 = getFolder("folder2", folder1, currentAuthenticatedUser);

            folderRepository.save(folder);
            folderRepository.save(folder1);
            folderRepository.save(folder2);

            MoveItemRequest moveItemRequest = new MoveItemRequest(folder.getId());
            String moveItemRequestJson = objectMapper.writeValueAsString(moveItemRequest);

            mockMvc.perform(MockMvcRequestBuilders.patch("/api/items/move/" + folder2.getId())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(moveItemRequestJson)
            ).andExpect(status().isNoContent());

            FolderDto movedFolder = folderService.findFolderById(folder1.getId());

            FolderDto parentFolderDto = movedFolder.getFolder();
            assertThat(parentFolderDto.getId()).isEqualTo(folder.getId());
        }


        @Test
        public void shouldMoveFolderToSubfolderThrowConflict() throws Exception {
            Folder folder = getFolder("folder", null, currentAuthenticatedUser);
            Folder folder1 = getFolder("folder1", folder, currentAuthenticatedUser);

            folderRepository.save(folder);
            folderRepository.save(folder1);

            MoveItemRequest moveItemRequest = new MoveItemRequest(folder1.getId());
            String moveItemRequestJson = objectMapper.writeValueAsString(moveItemRequest);

            mockMvc.perform(MockMvcRequestBuilders.patch("/api/items/move/" + folder.getId())
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(moveItemRequestJson)
            ).andExpect(status().isConflict());

        }
    }


    @Test
    public void shouldItemPathBeUpdatedIfMoved() throws Exception {
        FolderRequest folderRequest = getFolderRequest("folder", null);
        FolderDto folderDto = folderService.createFolder(folderRequest);

        FolderRequest folderRequest1 = getFolderRequest("folder1", folderDto.getId());
        FolderDto folderDto1 = folderService.createFolder(folderRequest1);

        assertThat(folderDto1.getPath()).isEqualTo("/folder/folder1");

        MoveItemRequest moveItemRequest = new MoveItemRequest(null);
        String moveItemRequestJson = objectMapper.writeValueAsString(moveItemRequest);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/items/move/" + folderDto1.getId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(moveItemRequestJson)
        ).andExpect(status().isNoContent());

        Optional<Folder> folder = folderRepository.findById(folderDto1.getId());
        assertThat(folder).isPresent();

        assertThat(folder.get().getPath()).isEqualTo("/folder1");
    }

}
