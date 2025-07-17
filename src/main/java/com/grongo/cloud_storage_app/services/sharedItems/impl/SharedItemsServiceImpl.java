package com.grongo.cloud_storage_app.services.sharedItems.impl;


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
    public void sharedItem(SharedItemRequest sharedItemRequest) {
        Long itemId = sharedItemRequest.getItemId();

        User authenticatedUser = authService.getCurrentAuthenticatedUser();
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Could not find item with id " + itemId));
        storageService.checkItemPermission(item, authenticatedUser);

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

}
