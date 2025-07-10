package com.grongo.cloud_storage_app.services.items.impl;

import com.grongo.cloud_storage_app.exceptions.storageExceptions.FolderNotFoundException;
import com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.FolderDto;
import com.grongo.cloud_storage_app.models.items.dto.FolderRequest;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.ItemRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.security.CustomUserDetails;
import com.grongo.cloud_storage_app.services.items.FolderService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public FolderDto createFolder(FolderRequest folderRequest) {

        Long parentFolderId = folderRequest.getParentFolderId();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = ((CustomUserDetails) authentication.getPrincipal()).getUser();

        Folder parentFolder = null;

        if (parentFolderId != null){
            parentFolder = folderRepository.findById(parentFolderId).orElseThrow(() -> new FolderNotFoundException("Could not find folder with id " + parentFolderId));
        }

        List<Item> items = getItemsInFolder(parentFolderId, user.getId());

        //throw if any conflict with the names
        items.forEach(item -> {
            if (item.getName().equals(folderRequest.getName())){
                throw new FolderNotFoundException("There is already a folder or file named " + folderRequest.getName());
            }
        });

        Folder folder = Folder.builder().name(folderRequest.getName()).folder(parentFolder).owner(user).build();

        folderRepository.save(folder);

        return modelMapper.map(folder, FolderDto.class);

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
}
