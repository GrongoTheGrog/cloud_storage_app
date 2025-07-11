package com.grongo.cloud_storage_app.services.items;

import com.grongo.cloud_storage_app.models.items.Item;

import java.util.List;

public interface StorageService {
    void updatePath(Long itemId);
    void updatePath(Item item);
    void moveItem(Long itemId, Long newParentId);
    void renameItem(Long itemId, String name);
    List<Item> getItemsInFolder(Long id, Long userId);
}
