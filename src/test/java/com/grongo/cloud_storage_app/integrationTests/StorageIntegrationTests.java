package com.grongo.cloud_storage_app.integrationTests;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.dto.FolderRequest;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FileRepository;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.auth.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.grongo.cloud_storage_app.testUtils.TestUtils.*;

import java.nio.file.Path;
import java.util.List;

@AutoConfigureMockMvc
@SpringBootTest
@ExtendWith(MockitoExtension.class)
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
public class StorageIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtService jwtService;

    private String accessToken;
    private User currentAuthenticatedUser;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    //GET AN ACCESS TOKEN TO BE ABLE TO PERFORM REQUESTS
    public void authenticate(){
        currentAuthenticatedUser = User.builder()
                    .username("test")
                    .email("test")
                    .build();

        userRepository.save(currentAuthenticatedUser);
        accessToken = jwtService.createAccessToken(1L, "test").getAccessToken();
    }

    @Nested
    class FileTests {

        @MockitoSpyBean
        S3Client s3Client;

        @MockitoSpyBean
        S3Presigner s3Presigner;

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


            mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files")
                            .file(multipartFile)
                            .header("Authorization", "Bearer " + accessToken)

                    )
                    .andExpect(status().isCreated());

            verify(s3Client).putObject((PutObjectRequest) any(), (Path) any());
            List<File> fileList = fileRepository.findAll();
            assertThat(fileList).hasSize(1);

        }
    }


    @Nested
    class FolderTests{

        @Autowired
        FolderRepository folderRepository;

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
        public void testIfFolderCanBeMoved(){
            Folder folder = getFolder("folder", null, currentAuthenticatedUser);
            Folder folder1 = getFolder("folder1", folder, currentAuthenticatedUser);
            Folder folder2 = getFolder("folder2", folder1, currentAuthenticatedUser);

            folderRepository.save(folder);
            folderRepository.save(folder1);
            folderRepository.save(folder2);

        }
    }


}
