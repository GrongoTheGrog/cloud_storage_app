package com.grongo.cloud_storage_app.services.items;


import com.grongo.cloud_storage_app.models.items.dto.FolderDto;
import com.grongo.cloud_storage_app.models.items.dto.FolderRequest;
import com.grongo.cloud_storage_app.models.items.dto.ItemDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FolderService {

    /**Creates a folder and saves it to the database
     *
     * @param folderRequest the request body format for creating a folder
     * @return FolderDto that contains the folder metadata
     * @throws com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException if authenticated user does not exist
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.FolderNotFoundException if folder can't be found
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.ConflictStorageException if there is a name conflict
     */
    FolderDto createFolder(FolderRequest folderRequest);

    /**
     * Finds a folder by it's id
     *
     * @param id the folder id
     * @return folder dto
     */
    FolderDto findFolderById(Long id);

    /**
     *
     * Looks for all items in a given folder
     * @param folderId the folder id to look into
     * @return a list of items
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.FolderNotFoundException if folder can't be found with given id
     */
    List<ItemDto> openFolder(Long folderId);

    /**
     * deletes the folder by the given id
     * @param folderId the id of the folder to be deleted
     */
    void deleteFolder(Long folderId);

}
