package com.grongo.cloud_storage_app.services.items;


import com.grongo.cloud_storage_app.models.items.dto.FolderDto;
import com.grongo.cloud_storage_app.models.items.dto.FolderRequest;

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
     * @return An optional of a FolderDto
     */
    Optional<FolderDto> findFolderById(Long id);
}
