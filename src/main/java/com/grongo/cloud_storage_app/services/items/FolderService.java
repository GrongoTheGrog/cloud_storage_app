package com.grongo.cloud_storage_app.services.items;


import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.dto.*;

import java.util.List;

public interface FolderService {

    FolderDto createFolder(FolderRequest folderRequest);
    GetFolderResponse findFolderById(Long id);
    List<ItemDto> openFolder(Long folderId);
    void deleteFolder(Folder folder);

}
