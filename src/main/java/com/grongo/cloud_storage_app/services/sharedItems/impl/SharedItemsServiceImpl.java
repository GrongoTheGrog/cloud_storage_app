package com.grongo.cloud_storage_app.services.sharedItems.impl;


import com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException;
import com.grongo.cloud_storage_app.exceptions.sharedItemsException.DuplicateSharedItemException;
import com.grongo.cloud_storage_app.exceptions.sharedItemsException.TargetEmailConflictException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.ItemNotFoundException;
import com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.sharedItems.SharedItem;
import com.grongo.cloud_storage_app.models.sharedItems.dto.SharedItemRequest;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.ItemRepository;
import com.grongo.cloud_storage_app.repositories.SharedItemRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.grongo.cloud_storage_app.services.sharedItems.FilePermission;
import com.grongo.cloud_storage_app.services.sharedItems.FileRole;
import com.grongo.cloud_storage_app.services.sharedItems.SharedItemsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SharedItemsServiceImpl implements SharedItemsService {

    private final AuthService authService;
    private final StorageService storageService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final SharedItemRepository sharedItemRepository;

    @Override
    public void createSharedItem(SharedItemRequest sharedItemRequest) {
        Long itemId = sharedItemRequest.getItemId();

        User authenticatedUser = authService.getCurrentAuthenticatedUser();
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Could not find item with id " + itemId));


        if (sharedItemRequest.getFileRole() == FileRole.ADMIN_MODE && !authenticatedUser.equals(item.getOwner())){
            throw new AccessDeniedException("User has to be the resource owner to grant admin permissions.");
        }

        storageService.checkItemPermission(item, authenticatedUser, FilePermission.SHARE);



        User foundTargetUser = userRepository.findByEmail(sharedItemRequest.getEmail()).orElseThrow(() ->
                new UserNotFoundException("Could not find email with email " + sharedItemRequest.getEmail()
            )
        );

        if (authenticatedUser.getEmail().equals(sharedItemRequest.getEmail())) {
            throw new TargetEmailConflictException("Provided email is the same as the owner email.");
        }

        Optional<SharedItem> foundSharedItem = sharedItemRepository.findByItemAndUser(item.getId(), authenticatedUser.getId());

        if (foundSharedItem.isPresent()) {
            throw new DuplicateSharedItemException("The given resource has already been shared with the email " + sharedItemRequest.getEmail());
        }

        SharedItem sharedItem = SharedItem.builder()
                .owner(authenticatedUser)
                .user(foundTargetUser)
                .fileRole(sharedItemRequest.getFileRole())
                .item(item)
                .build();

        sharedItemRepository.save(sharedItem);
    }

    @Override
    public void updateSharedItem(SharedItemRequest sharedItemRequest, Long id){
        Optional<SharedItem> sharedItem = sharedItemRepository.findById(id);
        if (sharedItem.isEmpty()){
            createSharedItem(sharedItemRequest);
            return;
        }

        User authenticatedUser = authService.getCurrentAuthenticatedUser();
        Item item = itemRepository.findById(sharedItemRequest.getItemId()).orElseThrow(() -> new ItemNotFoundException("Could not find item."));

        if (item.getOwner().getEmail().equals(sharedItemRequest.getEmail())){
            throw new TargetEmailConflictException("Provided email is the same as the owner email.");
        }

        //  FIRST CHECK IF THE USER HAS ACCESS OVER THE NEW FILE
        if (sharedItemRequest.getFileRole() == FileRole.ADMIN_MODE && !authenticatedUser.equals(item.getOwner())){
            throw new AccessDeniedException("User has to be the resource owner to grant admin permissions.");
        }
        storageService.checkItemPermission(item, authenticatedUser, FilePermission.SHARE);

        //  THEN CHECK IF THE USER HAS ACCESS OVER THE PREVIOUS FILE
        if (!sharedItem.get().getItem().equals(item)){
            storageService.checkItemPermission(sharedItem.get().getItem(), authenticatedUser, FilePermission.SHARE);
        }


        sharedItem.get().setItem(item);
        sharedItem.get().setUser(authenticatedUser);
        sharedItem.get().setFileRole(sharedItemRequest.getFileRole());

        sharedItemRepository.save(sharedItem.get());
    }

    @Override
    public void deleteSharedItem(Long id){
        User user = authService.getCurrentAuthenticatedUser();
        Optional<SharedItem> sharedItem = sharedItemRepository.findById(id);

        if (sharedItem.isEmpty()) return;
        storageService.checkItemPermission(sharedItem.get().getItem(), user, FilePermission.SHARE);

        sharedItemRepository.delete(sharedItem.get());
    }
}
