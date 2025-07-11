package com.grongo.cloud_storage_app.services.items;


import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.FolderDto;
import com.grongo.cloud_storage_app.models.items.dto.FolderRequest;

import java.util.List;
import java.util.Optional;

public interface FolderService {

    FolderDto createFolder(FolderRequest folderRequest);
    Optional<FolderDto> findFolderById(Long id);
}
