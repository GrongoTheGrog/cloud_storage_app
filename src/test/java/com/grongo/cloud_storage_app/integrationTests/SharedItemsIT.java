package com.grongo.cloud_storage_app.integrationTests;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.MoveItemRequest;
import com.grongo.cloud_storage_app.models.sharedItems.SharedItem;
import com.grongo.cloud_storage_app.models.sharedItems.dto.SharedItemRequest;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.*;
import com.grongo.cloud_storage_app.services.items.impl.ItemService;
import com.grongo.cloud_storage_app.services.jwt.JwtAccessService;
import com.grongo.cloud_storage_app.services.jwt.JwtRefreshService;
import com.grongo.cloud_storage_app.services.sharedItems.FileRole;
import com.grongo.cloud_storage_app.services.sharedItems.SharedItemsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import static com.grongo.cloud_storage_app.TestUtils.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SharedItemsIT {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private JwtRefreshService jwtRefreshService;
    @Autowired
    private JwtAccessService jwtAccessService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    FolderRepository folderRepository;

    @Autowired
    SharedItemRepository sharedItemRepository;

    @Autowired
    SharedItemsService sharedItemsService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    FileRepository fileRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    String accessToken;
    String sharingUserAccessToken;
    User currentAuthenticatedUser;
    User sharingUser;

    @BeforeEach
    public void createUsers(){
        currentAuthenticatedUser = User.builder().email("test1@gmail.com").password("123").username("test1").build();
        sharingUser = User.builder().email("test2@gmail.com").password("123").username("test2").build();

        userRepository.save(currentAuthenticatedUser);
        userRepository.save(sharingUser);

        accessToken = jwtAccessService.create(currentAuthenticatedUser.getId(), currentAuthenticatedUser.getEmail());
        sharingUserAccessToken = jwtAccessService.create(sharingUser.getId(), sharingUser.getEmail());
    }

    @Test
    public void testIfFolderCanBeSharedWithAnotherUser() throws Exception {
        Folder folder = Folder.builder()
                .folder(null)
                .name("folder")
                .owner(currentAuthenticatedUser)
                .path("/folder")
                .build();

        folderRepository.save(folder);

        SharedItemRequest sharedItemRequest = SharedItemRequest.builder()
                .itemId(folder.getId())
                .email(sharingUser.getEmail())
                .fileRole(FileRole.ADMIN_ROLE)
                .build();

        String sharedItemRequestJson = objectMapper.writeValueAsString(sharedItemRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/sharedItems")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(sharedItemRequestJson)
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isNoContent());


        List<SharedItem> sharedItemList = sharedItemRepository.findAll();

        assertThat(sharedItemList.size()).isEqualTo(1);
        assertThat(sharedItemList.getFirst().getUser().getId()).isEqualTo(sharingUser.getId());
        assertThat(sharedItemList.getFirst().getOwner().getId()).isEqualTo(currentAuthenticatedUser.getId());
    }


    @Test
    public void testIfFileCanBeSharedWithAnotherUser() throws Exception {
        File file = File.builder()
                .folder(null)
                .name("file")
                .owner(currentAuthenticatedUser)
                .path("/file")
                .build();

        fileRepository.save(file);

        SharedItemRequest sharedItemRequest = SharedItemRequest.builder()
                .itemId(file.getId())
                .email(sharingUser.getEmail())
                .fileRole(com.grongo.cloud_storage_app.services.sharedItems.FileRole.ADMIN_ROLE)
                .build();

        String sharedItemRequestJson = objectMapper.writeValueAsString(sharedItemRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/sharedItems")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(sharedItemRequestJson)
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isNoContent());


        List<SharedItem> sharedItemList = sharedItemRepository.findAll();

        assertThat(sharedItemList.size()).isEqualTo(1);
        assertThat(sharedItemList.getFirst().getUser().getId()).isEqualTo(sharingUser.getId());
        assertThat(sharedItemList.getFirst().getOwner().getId()).isEqualTo(currentAuthenticatedUser.getId());
    }

    @Test
    public void testIfSharedFolderCanBeOpenedByAnotherUser() throws Exception {
        Folder folder = getFolder("folder", null, currentAuthenticatedUser);
        folderRepository.save(folder);

        SharedItem sharedItem = getSharedItem(folder, sharingUser, currentAuthenticatedUser, com.grongo.cloud_storage_app.services.sharedItems.FileRole.VIEW_ROLE);
        sharedItemRepository.save(sharedItem);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/folders/open/" + folder.getId())
                .with(csrf())
                .header("Authorization", "Bearer " + sharingUserAccessToken)
        ).andExpect(status().isOk());

    }

    @Test
    public void testIfNestedItemsOfSharedFoldersCanBeAccessed() throws Exception {
        Folder sharedFolder = getFolder("sharedFolder", null, currentAuthenticatedUser);
        sharedFolder.setPath("/sharedFolder");
        folderRepository.save(sharedFolder);

        Folder nestedFolder = getFolder("nestedFolder", sharedFolder, currentAuthenticatedUser);
        nestedFolder.setPath("/sharedFolder/nestedFolder");
        folderRepository.save(nestedFolder);

        SharedItem sharedItem = getSharedItem(sharedFolder, sharingUser, currentAuthenticatedUser, com.grongo.cloud_storage_app.services.sharedItems.FileRole.VIEW_ROLE);
        sharedItemRepository.save(sharedItem);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/folders/open/" + nestedFolder.getId())
                .with(csrf())
                .header("Authorization", "Bearer " + sharingUserAccessToken)
        ).andExpect(status().isOk());
    }

    @Test
    public void testIfSystemBlocksUsersWithWrongPermissions() throws Exception {
        Folder sharedFolder = getFolder("sharedFolder", null, currentAuthenticatedUser);
        sharedFolder.setPath("/sharedFolder");
        folderRepository.save(sharedFolder);

        SharedItem sharedItem = getSharedItem(sharedFolder, sharingUser, currentAuthenticatedUser, com.grongo.cloud_storage_app.services.sharedItems.FileRole.VIEW_ROLE);
        sharedItemRepository.save(sharedItem);

        MoveItemRequest moveItemRequest = MoveItemRequest.builder().newFolderId(null).build();

        mockMvc.perform(MockMvcRequestBuilders
                .patch("/api/items/move/" + sharedFolder.getId())
                .with(csrf())
                .header("Authorization", "Bearer " + sharingUserAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moveItemRequest))
        ).andExpect(status().isForbidden());
    }

    @Test
    public void testIfSharedItemsCanBeUpdated() throws Exception {
        Folder sharedFolder = getFolder("sharedFolder", null, currentAuthenticatedUser);
        sharedFolder.setPath("/sharedFolder");
        folderRepository.save(sharedFolder);

        Folder updatedSharedFolder = getFolder("updatedSharedFolder", null, currentAuthenticatedUser);
        updatedSharedFolder.setPath("/updatedSharedFolder");
        folderRepository.save(updatedSharedFolder);

        SharedItem sharedItem = getSharedItem(sharedFolder, sharingUser, currentAuthenticatedUser, com.grongo.cloud_storage_app.services.sharedItems.FileRole.VIEW_ROLE);
        sharedItemRepository.save(sharedItem);

        SharedItemRequest sharedItemRequest = SharedItemRequest.builder()
                .email(sharingUser.getEmail())
                .itemId(updatedSharedFolder.getId())
                .fileRole(FileRole.EDIT_ROLE)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/sharedItems/" + sharedItem.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sharedItemRequest))
                .with(csrf())
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isNoContent());


        List<SharedItem> sharedItemList = sharedItemRepository.findAll();

        assertThat(sharedItemList.size()).isEqualTo(1);
        assertThat(sharedItemList.getFirst().getItem().getName()).isEqualTo(updatedSharedFolder.getName());
        assertThat(sharedItemList.getFirst().getFileRole()).isEqualTo(FileRole.EDIT_ROLE);
    }

    @Test
    public void TestIfSharingResourceCanBeDeleted() throws Exception {
        Folder sharedFolder = getFolder("sharedFolder", null, currentAuthenticatedUser);
        sharedFolder.setPath("/sharedFolder");
        folderRepository.save(sharedFolder);

        SharedItem sharedItem = getSharedItem(sharedFolder, sharingUser, currentAuthenticatedUser, com.grongo.cloud_storage_app.services.sharedItems.FileRole.VIEW_ROLE);
        sharedItemRepository.save(sharedItem);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/sharedItems/" + sharedItem.getId())
                .header("Authorization", "Bearer " + accessToken)
                .with(csrf())
        ).andExpect(status().isNoContent());

        List<SharedItem> sharedItemList = sharedItemRepository.findAll();

        assertThat(sharedItemList.isEmpty()).isTrue();
    }

    @Test
    public void testIfSharedItemsUserCanBeReturned(){

        Item item = Item.builder().owner(sharingUser).build();

        itemRepository.save(item);

        SharedItem sharedItem = SharedItem.builder()
                .item(item)
                .user(currentAuthenticatedUser)
                .owner(sharingUser)
                .build();

        sharedItemRepository.save(sharedItem);

        List<SharedItem> ownerList = sharedItemRepository.getAllSharingItems(sharingUser.getId());
        List<SharedItem> userList = sharedItemRepository.getAllSharingItems(currentAuthenticatedUser.getId());

        assertThat(userList.size()).isEqualTo(1);
        assertThat(ownerList.size()).isEqualTo(1);

        assertThat(userList.getFirst().getOwner().getId()).isEqualTo(sharingUser.getId());
        assertThat(ownerList.getFirst().getUser().getId()).isEqualTo(currentAuthenticatedUser.getId());

    }
}
