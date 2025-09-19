package com.grongo.cloud_storage_app.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.dto.ItemVisibilityUpdateRequest;
import com.grongo.cloud_storage_app.models.items.dto.MoveItemRequest;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FileRepository;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.grongo.cloud_storage_app.services.jwt.JwtAccessService;
import com.grongo.cloud_storage_app.services.jwt.JwtRefreshService;
import com.grongo.cloud_storage_app.services.sharedItems.FilePermission;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.List;
import java.util.Optional;

import static com.grongo.cloud_storage_app.TestUtils.getFile;
import static com.grongo.cloud_storage_app.TestUtils.getFolder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FileIT {

    @Autowired
    FileRepository fileRepository;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StorageService storageService;
    @Autowired
    JwtAccessService jwtAccessService;
    @Autowired
    JwtRefreshService jwtRefreshService;
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

        accessToken = jwtAccessService.create(user.getId(), user.getEmail());
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
                .with(csrf())
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
                .with(csrf())
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
                .with(csrf())
                .content(objectMapper.writeValueAsString(request))
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isNoContent());

        Optional<File> fileAfterUpdate = fileRepository.findById(file.getId());
        assertThat(fileAfterUpdate).isPresent();
        assertThat(fileAfterUpdate.get().getIsPublic()).isTrue();
    }
}
