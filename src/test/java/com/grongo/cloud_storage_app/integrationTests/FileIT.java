package com.grongo.cloud_storage_app.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.dto.ItemVisibilityUpdateRequest;
import com.grongo.cloud_storage_app.models.items.dto.MoveItemRequest;
import com.grongo.cloud_storage_app.models.items.dto.UploadFileForm;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FileRepository;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.auth.JwtService;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.grongo.cloud_storage_app.services.sharedItems.FilePermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static com.grongo.cloud_storage_app.TestUtils.getFile;
import static com.grongo.cloud_storage_app.TestUtils.getFolder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FileIT {

    @Autowired
    FileRepository fileRepository;

    @MockitoSpyBean
    S3Client s3Client;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StorageService storageService;

    @Autowired
    JwtService jwtService;

    @Autowired
    FolderRepository folderRepository;

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


    @Test
    public void testIfFileCanBeUpdated() throws Exception {
        File beforeUpdateFile = getFile("beforeUpdateFile", null, currentAuthenticatedUser);
        fileRepository.save(beforeUpdateFile);

        MockMultipartFile afterUploadFile = new MockMultipartFile(
                "file",
                "afterUploadFile.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test".getBytes()
        );

        UploadFileForm uploadFileForm = UploadFileForm.builder()
                .file(afterUploadFile)
                .fileName("newFileName")
                .folderId(null)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/files/" + beforeUpdateFile.getId())
                .file(afterUploadFile)
                .param("fileName", "newFileName")
                .header("Authorization", "Bearer " + accessToken)
                .with(request -> {request.setMethod("PUT"); return request;})

        ).andExpect(status().isNoContent());

        verify(s3Client).putObject((PutObjectRequest) any(), (Path) any());
        List<File> fileList = fileRepository.findAll();
        assertThat(fileList.isEmpty()).isFalse();
        assertThat(fileList.getFirst().getName()).isEqualTo("newFileName");
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

    @Test
    public void testIfPublicFileCanBeAccessed() throws Exception {
        User resourceOwner = User.builder().email("owner").username("owner").build();
        userRepository.save(resourceOwner);

        File file = getFile("file", null, resourceOwner);
        file.setIsPublic(true);
        fileRepository.save(file);

        storageService.checkItemPermission(file, currentAuthenticatedUser, FilePermission.VIEW);
    }


    @Test
    public void testIfFileVisibilityCanBeUpdated() throws Exception {
        File file = getFile("file", null, currentAuthenticatedUser);
        fileRepository.save(file);

        ItemVisibilityUpdateRequest request = new ItemVisibilityUpdateRequest(true);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/items/visibility/" + file.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isNoContent());

        Optional<File> fileAfterUpdate = fileRepository.findById(file.getId());
        assertThat(fileAfterUpdate).isPresent();
        assertThat(fileAfterUpdate.get().getIsPublic()).isTrue();
    }
}
