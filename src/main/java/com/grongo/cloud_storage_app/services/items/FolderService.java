package com.grongo.cloud_storage_app.services.items;


import com.grongo.cloud_storage_app.models.items.dto.FolderDto;
import com.grongo.cloud_storage_app.models.items.dto.FolderNestedDto;
import com.grongo.cloud_storage_app.models.items.dto.FolderRequest;
import com.grongo.cloud_storage_app.models.items.dto.ItemDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FolderService {

    FolderDto createFolder(FolderRequest folderRequest);
    FolderNestedDto findFolderById(Long id);
    List<ItemDto> openFolder(Long folderId);
    void deleteFolder(Long folderId);

}
