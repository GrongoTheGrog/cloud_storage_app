package com.grongo.cloud_storage_app.services.items.impl;


import com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.ConflictStorageException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.FolderNotFoundException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.ItemNotFoundException;
import com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.ItemRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.items.FolderService;
import com.grongo.cloud_storage_app.services.items.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class StorageServiceImpl implements StorageService {

    final private ItemRepository itemRepository;
    final private FolderRepository folderRepository;
    final private UserRepository userRepository;
    final private AuthService authService;

    @Override
    public void updatePath(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Could not find item with id of " + itemId));

        updatePath(item);
    }

    @Override
    public void updatePath(Item item){
        Folder parentFolder = item.getFolder();
//        List<String> path = new ArrayList<>();
//        while (current != null){
//            String currentLocation = "/" + current.getName();
//            path.add(currentLocation);
//            current = current.getFolder();
//        }
//
//        path = path.reversed();
//        path.add("/" + item.getName());
//        item.setPath(String.join("", path));

        String path = "";

        if (parentFolder != null){
            path = parentFolder.getPath();
        }

        path += "/" + item.getName();

        itemRepository.save(item);


        // BREADTH FIRST TRAVERSAL ON FILE TREE
        if(item instanceof Folder folder){
            Queue<Item> items = new ArrayDeque<>();
            items.add(folder);

            while (!items.isEmpty()){
                Item polled = items.poll();
                Item parentItem = polled.getFolder();

                String currentPath = (parentItem == null ? "" : parentItem.getPath()) + "/" + polled.getName();
                polled.setPath(currentPath);

                itemRepository.save(polled);

                if (polled instanceof Folder nestedFolder){
                    List<Item> itemList = nestedFolder.getStoredFiles();
                    if (itemList != null){
                        items.addAll(itemList);
                    }
                }
            }
        }
    }



    @Override
    public void moveItem(Long itemId, Long newParentId) {
        User user = authService.getCurrentAuthenticatedUser();

        Item foundItem = itemRepository
                .findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Could not find item with id of " + itemId));

        if (!user.getId().equals(foundItem.getOwner().getId())) throw new AccessDeniedException("Authenticated user is not the owner of the item.");

        Folder folder = null;
        if (newParentId != null){
            folder = folderRepository
                    .findById(newParentId)
                    .orElseThrow(() -> new FolderNotFoundException("Could not find folder with id of " + newParentId));

            if (checkNameConflict(folder.getId(), user.getId(), foundItem.getName())){
                throw new ConflictStorageException("There is already a file named " + foundItem.getName() + " in the given directory.");
            }
            foundItem.setFolder(folder);
            updatePath(foundItem);
        }


    }

    @Override
    public void renameItem(Long itemId, String name) {
        Item foundItem = itemRepository
                .findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Could not find item with id of " + itemId));

        Long parentFolderId = foundItem.getFolder() == null ? null : foundItem.getFolder().getId();
        User user = authService.getCurrentAuthenticatedUser();

        if (!user.getId().equals(foundItem.getOwner().getId())) throw new AccessDeniedException("Authenticated user is not the owner of the file.");

        List<Item> itemList = getItemsInFolder(parentFolderId, user.getId());

        boolean conflict = itemList.stream().anyMatch(item -> item.getName().equals(name));

        if (conflict) throw new ConflictStorageException("There is already a folder or file named " + name);

        foundItem.setName(name);
        itemRepository.save(foundItem);

        updatePath(foundItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Item> getItemsInFolder(Long id, Long userId) {
        if (id == null){
            return itemRepository.findAllRootItems(userId);
        }

        Folder folder = folderRepository.findById(id).orElseThrow(() -> new FolderNotFoundException("Could not find folder with id of " + id));

        return folder.getStoredFiles();
    }

    @Override
    public boolean checkNameConflict(Long folderId, Long userId, String itemName) {
        List<Item> itemList = getItemsInFolder(folderId, userId);
        return itemList.stream().anyMatch(item -> item.getName().equals(itemName));
    }
}
