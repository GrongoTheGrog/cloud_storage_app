package com.grongo.cloud_storage_app.services.items.impl;


import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.repositories.ItemRepository;
import com.grongo.cloud_storage_app.services.items.FileService;
import com.grongo.cloud_storage_app.services.items.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final FolderService folderService;
    private final FileService fileService;
    private final ItemRepository itemRepository;


    @Transactional
    public void deleteItems(List<Long> ids){
        ids.forEach(id -> {
            Optional<Item> item = itemRepository.findById(id);
            if (item.isEmpty()) return;

            if (item.get() instanceof Folder folder){
                folderService.deleteFolder(folder);
            }else if(item.get() instanceof File file){
                fileService.deleteFile(file);
            }
        });
    }
}
