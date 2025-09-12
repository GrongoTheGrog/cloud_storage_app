package com.grongo.cloud_storage_app.services.items.impl;

import com.grongo.cloud_storage_app.exceptions.storageExceptions.ConflictStorageException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.FolderNotFoundException;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.*;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.ItemRepository;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.items.FileService;
import com.grongo.cloud_storage_app.services.items.FolderService;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.grongo.cloud_storage_app.services.sharedItems.FilePermission;
import com.grongo.cloud_storage_app.services.sharedItems.FileRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Log4j2
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final ModelMapper modelMapper;
    private final StorageService storageService;
    private final AuthService authService;
    private final FileService fileService;
    private final ItemRepository itemRepository;
    private final FileChecker fileChecker;

    @Override
    public FolderDto createFolder(FolderRequest folderRequest) {

        Long parentFolderId = folderRequest.getParentFolderId();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = authService.getCurrentAuthenticatedUser();

        Folder parentFolder = null;

        if (parentFolderId != null){
            parentFolder = folderRepository.findById(parentFolderId).orElseThrow(() -> new FolderNotFoundException("Could not find folder with id " + parentFolderId));
        }

        if (storageService.checkNameConflict(parentFolderId, user.getId(), folderRequest.getName())){
            throw new ConflictStorageException("There is already a folder or file named " + folderRequest.getName());
        }

        Boolean isPublic = folderRequest.getIsPublic();
        if (isPublic == null){
            if (parentFolder == null){
                isPublic = false;
            }else {
                isPublic = parentFolder.getIsPublic();
            }
        }

        Folder folder = Folder.builder()
                .name(folderRequest.getName())
                .folder(parentFolder)
                .owner(user)
                .isPublic(isPublic)
                .size(0L)
                .build();

        folderRepository.save(folder);
        storageService.updatePath(folder);

        return modelMapper.map(folder, FolderDto.class);

    }

    @Override
    @Transactional(readOnly = true)
    public GetFolderResponse findFolderById(Long id) {
       User user = authService.getCurrentAuthenticatedUser();

        Folder folder;
        if (id == null){
            List<Item> items = itemRepository.findAllRootItems(user.getId());
            folder = Folder.builder()
                    .storedFiles(items)
                    .owner(user)
                    .path("/")
                    .name("Root")
                    .build();

            long size = 0;
            for (Item item : items){
                size += (item.getSize() != null ? item.getSize() : 0L);
            }

            folder.setSize(size);

        }else{
            folder = folderRepository.findById(id).orElseThrow(() -> new FolderNotFoundException("Could not find folder"));
        }

        FileRole fileRole = fileChecker.checkItemPermission(folder, user, FilePermission.VIEW);

        List<ItemDto> storedItems = folder.getStoredFiles()
                .stream()
                .map(item -> {
                    if (item instanceof File){
                        return modelMapper.map(item, FileDto.class);
                    }else{
                        return modelMapper.map(item, FolderDto.class);
                    }
                })
                .toList();

        folder.setStoredFiles(List.of());

        FolderNestedDto folderNestedDto = modelMapper.map(folder, FolderNestedDto.class);
        folderNestedDto.setStoredFiles(storedItems);
        return new GetFolderResponse(folderNestedDto, fileRole);
    }


    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> openFolder(Long folderId){
        User user = authService.getCurrentAuthenticatedUser();
        Long userId = user.getId();

        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new FolderNotFoundException("Couldn't find folder."));

        fileChecker.checkItemPermission(folder, user, FilePermission.VIEW);


        List<Item> itemList = storageService.getItemsInFolder(folder, userId);

        return itemList.stream().map(item -> {
            item.setFolder(null);
            return item instanceof File ?
                    modelMapper.map(item, FileDto.class) :
                    modelMapper.map(item, FolderDto.class);
        }).toList();
    }



    @Override
    @Transactional
    public void deleteFolder(Folder folder) {
        User authenticatedUser = authService.getCurrentAuthenticatedUser();

        fileChecker.checkItemPermission(folder, authenticatedUser, FilePermission.DELETE);

        List<Item> itemsToDelete = new ArrayList<>(folder.getStoredFiles());
        for (Item item : itemsToDelete) {
            if (item instanceof Folder f) {
                deleteFolder(f);
            } else if (item instanceof File file) {
                fileService.deleteFile(file);
            }
            folder.getStoredFiles().remove(item);
        }

        log.info("Deleting folder of id {}.", folder.getId());
        if (folder.getSize() != null && folder.getSize() > 0){
            storageService.updateSize(folder.getFolder(), -folder.getSize());
        }

        folderRepository.delete(folder);
    }


}
