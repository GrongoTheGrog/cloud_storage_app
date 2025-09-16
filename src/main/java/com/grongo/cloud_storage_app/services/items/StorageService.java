package com.grongo.cloud_storage_app.services.items;

import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.ItemDto;
import com.grongo.cloud_storage_app.models.items.dto.ItemVisibilityUpdateRequest;
import com.grongo.cloud_storage_app.models.items.dto.QueryItemDto;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.services.sharedItems.FilePermission;
import com.grongo.cloud_storage_app.services.sharedItems.FileRole;

import java.util.List;

/**
 * Class intended for operations that suit both files and folders
 *
 * @author grongo
 */
public interface StorageService {
    void updatePath(Long itemId);

    void updatePath(Item item);

    void moveItem(Long itemId, Long newParentId);

    void renameItem(Long itemId, String name);

    List<Item> getItemsInFolder(Long folderId, Long userId);

    List<Item> getItemsInFolder(Folder folder, Long userId);

    boolean checkNameConflict(Long folderId, Long userId, String itemName);

    boolean checkIfFolderIsAncestor(Folder ancestor, Folder child);

    void updateItemVisibility(ItemVisibilityUpdateRequest updateRequest, Long itemId);

    void updateSize(Item item, Long diff);

    void updateTreeSize(Item item, Long diff);

    List<ItemDto> queryFiles(QueryItemDto queryItemDto);

    void deleteItems(List<Long> ids);
}
