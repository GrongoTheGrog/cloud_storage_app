package com.grongo.cloud_storage_app.services.items.impl;


import com.grongo.cloud_storage_app.exceptions.HttpException;
import com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.ConflictStorageException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.FolderNotFoundException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.ItemNotFoundException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.StorageException;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.ItemDto;
import com.grongo.cloud_storage_app.models.items.dto.ItemVisibilityUpdateRequest;
import com.grongo.cloud_storage_app.models.items.dto.QueryItemDto;
import com.grongo.cloud_storage_app.models.sharedItems.SharedItem;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.*;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.cache.CacheKeys;
import com.grongo.cloud_storage_app.services.cache.impl.QueryRequestCache;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.grongo.cloud_storage_app.services.sharedItems.FilePermission;
import com.grongo.cloud_storage_app.services.sharedItems.FileRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StorageServiceImpl implements StorageService {

    final private ItemRepository itemRepository;
    final private FolderRepository folderRepository;
    final private AuthService authService;
    final private SharedItemRepository sharedItemRepository;
    final private ModelMapper modelMapper;
    final private QueryRequestCache queryRequestCache;
    final private FileChecker fileChecker;

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

            fileChecker.checkItemPermission(parentFolder, user, FilePermission.MOVE);

            updateSize(parentFolder, foundItem.getSize());
        }

        fileChecker.checkItemPermission(foundItem, user, FilePermission.MOVE);

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

        fileChecker.checkItemPermission(foundItem, user, FilePermission.UPDATE);

        log.info("Renaming item {} from {} to {}.", itemId, foundItem.getName(), name);

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
    public void updateItemVisibility(ItemVisibilityUpdateRequest itemVisibilityUpdateRequest, Long itemId){
        User authenticatedUser = authService.getCurrentAuthenticatedUser();
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Could not find item with id of " + itemId));

        fileChecker.checkItemPermission(item, authenticatedUser, FilePermission.SHARE);

        item.setIsPublic(itemVisibilityUpdateRequest.getIsPublic());
        itemRepository.save(item);
        log.info("Set isPublic of item {} to {}.", item.getId(), itemVisibilityUpdateRequest.isPublic);

        if (item instanceof Folder folder && folder.getStoredFiles() != null){
            Queue<Item> queue = new LinkedList<>(folder.getStoredFiles());

            while(!queue.isEmpty()){
                Item popped = queue.poll();
                popped.setIsPublic(itemVisibilityUpdateRequest.getIsPublic());
                log.info("Set isPublic of item {} to {}.", popped.getId(), itemVisibilityUpdateRequest.isPublic);
                itemRepository.save(popped);
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

    @Override
    public void updateTreeSize(Item item, Long diff) {
        Item current = item;
        while(current != null){
            updateSize(current, diff);
            current = current.getFolder();
        }
    }

    @Override
    public List<ItemDto> queryFiles(QueryItemDto queryItemDto) {
        User authenticatedUser = authService.getCurrentAuthenticatedUser();

        int hashedQueryDto = queryItemDto.hashCode();

        List<ItemDto> queryCache = queryRequestCache.getKeyList(CacheKeys.itemQueryKey(hashedQueryDto));
        if (!queryCache.isEmpty()) return queryCache;

        if (
                queryItemDto.getMaxBytes() != null &&
                queryItemDto.getMinBytes() != null &&
                queryItemDto.getMaxBytes() < queryItemDto.getMinBytes()
        ) throw new HttpException("Maximum file size can't be less then minimum file size.", HttpStatus.BAD_REQUEST);

        if (
                queryItemDto.getMaxDate() != null &&
                queryItemDto.getMinDate() != null &&
                queryItemDto.getMaxDate().isBefore(queryItemDto.getMinDate())
        ) throw new HttpException("Maximum creation date can't be before then minimum creation date.", HttpStatus.BAD_REQUEST);

        if (
                queryItemDto.getType() != null &&
                queryItemDto.getType().equals("FOLDER") &&
                queryItemDto.getFileType() != null
        ) throw new HttpException("File type can only be used for files.", HttpStatus.BAD_REQUEST);



        List<Item> itemList = itemRepository.queryItem(
                queryItemDto.getMaxDate(),
                queryItemDto.getMinDate(),
                queryItemDto.getMaxBytes(),
                queryItemDto.getMinBytes(),
                queryItemDto.getName(),
                queryItemDto.getFileType(),
                queryItemDto.getParentId(),
                queryItemDto.getTagId(),
                authenticatedUser.getId(),
                queryItemDto.getType()
        );

        List<Item> sharedItems = sharedItemRepository.querySharedItems(
                queryItemDto.getMaxDate(),
                queryItemDto.getMinDate(),
                queryItemDto.getMaxBytes(),
                queryItemDto.getMinBytes(),
                queryItemDto.getName(),
                queryItemDto.getFileType(),
                queryItemDto.getParentId(),
                queryItemDto.getTagId(),
                authenticatedUser.getId(),
                queryItemDto.getType()
        ).stream().map(SharedItem::getItem).toList();

        itemList.addAll(sharedItems);

        List<ItemDto> itemDtoList = itemList.stream().map(item -> modelMapper.map(item, ItemDto.class)).toList();

        if (!itemDtoList.isEmpty()){
            queryRequestCache.setKeyList(CacheKeys.itemQueryKey(hashedQueryDto), itemDtoList, Duration.ofMinutes(10));
        }


        return itemDtoList;
    }

    @Override
    public void deleteItems(List<Long> ids) {

    }
}
