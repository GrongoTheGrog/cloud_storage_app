package com.grongo.cloud_storage_app.services.items.impl;

import com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.sharedItems.SharedItem;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.SharedItemRepository;
import com.grongo.cloud_storage_app.services.sharedItems.FilePermission;
import com.grongo.cloud_storage_app.services.sharedItems.FileRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class FileChecker {

    private final SharedItemRepository sharedItemRepository;

    @Transactional(readOnly = true)
    public FileRole checkItemPermission(Item item, User user, FilePermission filePermission) {

        FileRole fileRole;
        if ((fileRole = isOwner(item, user)) != null) return fileRole;
        if ((fileRole = isItemShared(item, user, filePermission)) != null) return fileRole;
        if ((fileRole = isParentFolderShared(item, user, filePermission)) != null) return fileRole;
        if ((fileRole = isPublic(item)) != null) return fileRole;
        if ((fileRole = isParentFolderPublic(item)) != null) return fileRole;

        throw new AccessDeniedException("Missing required permissions.");

    }

    private FileRole isOwner(Item item, User user){
        if (user.getId().equals(item.getOwner().getId())) {
            return FileRole.ADMIN_ROLE;
        }
        return null;
    }

    private FileRole isItemShared(Item item, User user, FilePermission filePermission){
        Optional<SharedItem> sharedItem = sharedItemRepository.findByItemAndUser(item.getId(), user.getId());
        return sharedItem.map(SharedItem::getFileRole).orElse(null);
    }

    private FileRole isParentFolderShared(Item item, User user, FilePermission filePermission){
        List<SharedItem> sharedItems = sharedItemRepository.findByOwnerAndUser(item.getOwner().getId(), user.getId());

        if (!sharedItems.isEmpty()) {
            List<SharedItem> sharedItemFolderList = sharedItems.stream()
                    .filter(sharedItemMapped-> item.getPath().startsWith(sharedItemMapped.getItem().getPath()))
                    .sorted(Comparator.comparingInt( (SharedItem sharedItemSort) -> sharedItemSort.getItem().getPath().length()).reversed())
                    .toList();

            if (!sharedItemFolderList.isEmpty()) return sharedItemFolderList.getFirst().getFileRole();
        }

        return null;
    }

    private FileRole isPublic(Item item){
        return item.getIsPublic() ? FileRole.VIEW_ROLE : null;
    }

    private FileRole isParentFolderPublic(Item item){
        Folder current = item.getFolder();

        while(current != null){
            if (current.getIsPublic()) return FileRole.VIEW_ROLE;
            current = current.getFolder();
        }

        return null;
    }
}
