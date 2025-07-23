package com.grongo.cloud_storage_app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.FolderRequest;
import com.grongo.cloud_storage_app.models.sharedItems.SharedItem;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.models.user.dto.AuthenticateUser;
import com.grongo.cloud_storage_app.models.user.dto.RegisterUser;
import com.grongo.cloud_storage_app.services.sharedItems.FileRole;

public class TestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static RegisterUser getRequestUser(){
        return RegisterUser.builder()
                .username("test")
                .email("test@gmail.com")
                .password("test12345678")
                .build();
    }

    public static String getRequestUserJson() throws JsonProcessingException {
        RegisterUser registerUser = getRequestUser();
        return objectMapper.writeValueAsString(registerUser);
    }

    public static AuthenticateUser getAuthenticateUser(){
        return AuthenticateUser.builder()
                .email("test@gmail.com")
                .password("test12345678")
                .build();
    }

    public static String getAuthenticateUserJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(getAuthenticateUser());
    }

    public static Folder getFolder(String name, Folder parentFolder, User owner){
        return Folder.builder()
                .folder(parentFolder)
                .name(name)
                .owner(owner)
                .size(0L)
                .build();

    }

    public static File getFile(String name, Folder parent, User owner){
        return File.builder()
                .owner(owner)
                .name(name)
                .folder(parent)
                .size(10L)
                .build();
    }

    public static FolderRequest getFolderRequest(String name, Long folderParentId){
        return FolderRequest.builder()
                .parentFolderId(folderParentId)
                .name(name)
                .build();
    }

    public static SharedItem getSharedItem(Item item, User user, User owner, FileRole fileRole){
        return SharedItem.builder()
                .item(item)
                .user(user)
                .owner(owner)
                .fileRole(fileRole)
                .build();
    }
}
