package com.grongo.cloud_storage_app.services.items.impl;

import com.grongo.cloud_storage_app.exceptions.storageExceptions.ConflictStorageException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.FolderNotFoundException;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.FileDto;
import com.grongo.cloud_storage_app.models.items.dto.FolderDto;
import com.grongo.cloud_storage_app.models.items.dto.FolderRequest;
import com.grongo.cloud_storage_app.models.items.dto.ItemDto;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.ItemRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.cache.impl.OpenFolderCache;
import com.grongo.cloud_storage_app.services.items.FolderService;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.grongo.cloud_storage_app.services.sharedItems.FilePermissions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final ModelMapper modelMapper;
    private final StorageService storageService;
    private final AuthService authService;
    private final OpenFolderCache openFolderCache;

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

        Folder folder = Folder.builder().name(folderRequest.getName()).folder(parentFolder).owner(user).build();

        folderRepository.save(folder);
        storageService.updatePath(folder);

        return modelMapper.map(folder, FolderDto.class);

    }

    @Override
    @Transactional(readOnly = true)
    public FolderDto findFolderById(Long id) {
        Folder folder = folderRepository.findById(id).orElseThrow(() -> new FolderNotFoundException("Could not find folder of id " + id));
        User user = authService.getCurrentAuthenticatedUser();

        storageService.checkItemPermission(folder, user, FilePermissions.VIEW);

        return modelMapper.map(folder, FolderDto.class);
    }


    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> openFolder(Long folderId){
        User user = authService.getCurrentAuthenticatedUser();
        Long userId = user.getId();

        List<Item> itemList = storageService.getItemsInFolder(folderId, userId);


        return itemList.stream().map(item -> {
            item.setFolder(null);
            return item instanceof File ?
                    modelMapper.map(item, FileDto.class) :
                    modelMapper.map(item, FolderDto.class);
        }).toList();
    }

    @Override
    public void deleteFolder(Long folderId) {
        log.info("Deleting folder {}.", folderId);
        folderRepository.deleteById(folderId);
    }


}
