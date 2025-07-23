package com.grongo.cloud_storage_app.services.items.impl;


import com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.ConflictStorageException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.FolderNotFoundException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.ItemNotFoundException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.StorageException;
import com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.ItemVisibilityUpdateRequest;
import com.grongo.cloud_storage_app.models.sharedItems.SharedItem;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.ItemRepository;
import com.grongo.cloud_storage_app.repositories.SharedItemRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.cache.impl.OpenFolderCache;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.grongo.cloud_storage_app.services.sharedItems.FilePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class StorageServiceImpl implements StorageService {

    final private ItemRepository itemRepository;
    final private FolderRepository folderRepository;
    final private AuthService authService;
    final private OpenFolderCache openFolderCache;
    final private UserRepository userRepository;
    final private SharedItemRepository sharedItemRepository;

    @Override
    public void updatePath(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Could not find item with id of " + itemId));

        updatePath(item);
    }

    @Override
    @Transactional
    public void updatePath(Item item){
        Folder parentFolder = item.getFolder();

        String path = "";

        if (parentFolder != null){
            path = parentFolder.getPath();
        }

        path += "/" + item.getName();
        item.setPath(path);

        itemRepository.save(item);


        // GO UP THE FILE TREE
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
    @Transactional
    public void moveItem(Long itemId, Long newParentId) {
        User user = authService.getCurrentAuthenticatedUser();

        Item foundItem = itemRepository
                .findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Could not find item with id of " + itemId));


        updateSize(foundItem.getFolder(), -foundItem.getSize());

        Folder parentFolder = null;
        if (newParentId != null){
            parentFolder = folderRepository
                    .findById(newParentId)
                    .orElseThrow(() -> new FolderNotFoundException("Could not find folder with id of " + newParentId));

            checkItemPermission(parentFolder, user, FilePermission.MOVE);

            updateSize(parentFolder, foundItem.getSize());
        }

        checkItemPermission(foundItem, user, FilePermission.MOVE);

        //In case the resource is a folder, check if the new parent is not one of its subfolders
        if (foundItem.getType().equals("FOLDER") && checkIfFolderIsAncestor((Folder) foundItem, parentFolder)){
            throw new StorageException("You can't move a folder to one of it's children folders.", HttpStatus.CONFLICT);
        }

        if (checkNameConflict(parentFolder == null ? null : parentFolder.getId(), user.getId(), foundItem.getName())){
            throw new ConflictStorageException("There is already a file named " + foundItem.getName() + " in the given directory.");
        }

        foundItem.setFolder(parentFolder);
        updatePath(foundItem);
    }

    @Override
    public void renameItem(Long itemId, String name) {
        Item foundItem = itemRepository
                .findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Could not find item with id of " + itemId));

        Long parentFolderId = foundItem.getFolder() == null ? null : foundItem.getFolder().getId();
        User user = authService.getCurrentAuthenticatedUser();

        checkItemPermission(foundItem, user, FilePermission.UPDATE);

        List<Item> itemList = getItemsInFolder(parentFolderId, user.getId());

        boolean conflict = itemList.stream().anyMatch(item -> item.getName().equals(name));

        if (conflict) throw new ConflictStorageException("There is already a folder or file named " + name);

        foundItem.setName(name);
        updatePath(foundItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Item> getItemsInFolder(Long id, Long userId) {
        Folder folder = id != null ?
                folderRepository.findByIdWithStoredFiles(id).orElseThrow(() -> new FolderNotFoundException("Folder can't be found.")) :
                null;

        return getItemsInFolder(folder, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Item> getItemsInFolder(Folder folder, Long userId) {
        if (folder == null){
            return itemRepository.findAllRootItems(userId);
        }

        return folder.getStoredFiles();
    }


    @Override
    public boolean checkNameConflict(Long folderId, Long userId, String itemName) {
        List<Item> itemList = getItemsInFolder(folderId, userId);
        return itemList.stream().anyMatch(item -> item.getName().equals(itemName));
    }

    @Override
    public boolean checkIfFolderIsAncestor(Folder ancestor, Folder child) {
        Folder current = child;

        while (current != null){
            if (Objects.equals(ancestor.getId(), current.getId())) return true;
            current = current.getFolder();
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public void checkItemPermission(Item item, User user, FilePermission filePermission) {

        if (Boolean.TRUE.equals(item.getIsPublic()) && filePermission.equals(FilePermission.VIEW)) return;

        if (!user.getId().equals(item.getOwner().getId())) {

            //first check if item is directly shared with user
            Optional<SharedItem> sharedItem = sharedItemRepository.findByItemAndUser(item.getId(), user.getId());
            if (sharedItem.isPresent()){
                if (!sharedItem.get().getFileRole().hasPermission(filePermission)){
                    throw new AccessDeniedException("User doesn't have permission to do such action.");
                }
                return;
            }

            //or search for a shared folder
            List<SharedItem> sharedItems = sharedItemRepository.findByOwnerAndUser(item.getOwner().getId(), user.getId());

            if (sharedItems.isEmpty()) throw new AccessDeniedException("User doesn't have permission to do such action.");

            List<SharedItem> sharedItemFolderList = sharedItems.stream()
                    .filter(sharedItemMapped-> item.getPath().startsWith(sharedItemMapped.getItem().getPath()))
                    .sorted(Comparator.comparingInt( (SharedItem sharedItemSort) -> sharedItemSort.getItem().getPath().length()).reversed())
                    .toList();

            if (sharedItemFolderList.isEmpty() || !sharedItemFolderList.getFirst().getFileRole().hasPermission(filePermission)) {
                throw new AccessDeniedException("User doesn't have permission to do such action.");
            }
        }
    }

    @Override
    public void updateItemVisibility(ItemVisibilityUpdateRequest itemVisibilityUpdateRequest, Long itemId){
        User authenticatedUser = authService.getCurrentAuthenticatedUser();
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Could not find item with id of " + itemId));

        checkItemPermission(item, authenticatedUser, FilePermission.SHARE);

        item.setIsPublic(itemVisibilityUpdateRequest.getIsPublic());

        // propagate to children if folder
        if (item instanceof Folder folder && folder.getStoredFiles() != null){
            Queue<Item> queue = new LinkedList<>(folder.getStoredFiles());

            while(!queue.isEmpty()){
                Item popped = queue.poll();
                popped.setIsPublic(itemVisibilityUpdateRequest.getIsPublic());
                if (popped instanceof Folder subFolder && subFolder.getStoredFiles() != null){
                    queue.addAll(subFolder.getStoredFiles());
                }
            }
        }
    }

    @Override
    public void updateSize(Item item, Long diff){
        if (item != null){
            Long prevSize = item.getSize() == null ? 0 : item.getSize();
            item.setSize(prevSize + diff);
            itemRepository.save(item);
        }
    }


}
