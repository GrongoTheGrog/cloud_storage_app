package com.grongo.cloud_storage_app.integrationTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.FolderRequest;
import com.grongo.cloud_storage_app.models.items.dto.ItemVisibilityUpdateRequest;
import com.grongo.cloud_storage_app.models.items.dto.MoveItemRequest;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.auth.JwtService;
import com.grongo.cloud_storage_app.services.items.StorageService;
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

import static com.grongo.cloud_storage_app.TestUtils.getFolder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FolderIT {

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
    public void testIfFolderCanBeCreated() throws Exception {
        FolderRequest folderRequest = new FolderRequest("testFolder", null, false);
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

        Optional<Folder> movedFolder = folderRepository.findById(folder1.getId());

        assertThat(movedFolder).isPresent();
        assertThat(movedFolder.get().getFolder()).isNull();
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

        Optional<Folder> movedFolder = folderRepository.findById(folder1.getId());

        assertThat(movedFolder).isPresent();
        assertThat(movedFolder.get().getFolder()).isNotNull();
        assertThat(movedFolder.get().getFolder().getId()).isEqualTo(folder.getId());
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

    @Test
    public void shouldFolderBeAccessedIfPublic() throws Exception {
        User resourceOwner = User.builder().email("resourceOwnerTest").username("test").build();
        userRepository.save(resourceOwner);


        Folder folder = getFolder("folder", null, resourceOwner);
        folder.setIsPublic(true);

        folderRepository.save(folder);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/folders/open/" + folder.getId())
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());
    }

    @Test
    public void shouldFolderAndSubItemsChangeVisibilityIfFolderChanges() throws Exception {
        Folder rootFolder = getFolder("rootFolder", null, currentAuthenticatedUser);
        folderRepository.save(rootFolder);

        Folder subFolder1 = getFolder("subFolder1", rootFolder, currentAuthenticatedUser);
        Folder subFolder2 = getFolder("subFolder2", rootFolder, currentAuthenticatedUser);
        folderRepository.save(subFolder2);
        folderRepository.save(subFolder1);

        ItemVisibilityUpdateRequest request = new ItemVisibilityUpdateRequest(true);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/items/visibility/" + rootFolder.getId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isNoContent());

        Optional<Folder> rootFolderUpdated = folderRepository.findById(rootFolder.getId());
        assertThat(rootFolderUpdated).isPresent();

        List<Item> storedItems = storageService.getItemsInFolder(rootFolder.getId(), currentAuthenticatedUser.getId());
        assertThat(storedItems).hasSize(2);


        boolean areAllSubFoldersPublic = storedItems
                .stream()
                .allMatch(Item::getIsPublic);

        assertThat(areAllSubFoldersPublic).isTrue();
    }
}
