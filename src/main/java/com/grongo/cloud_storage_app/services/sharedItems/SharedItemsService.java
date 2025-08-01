package com.grongo.cloud_storage_app.services.sharedItems;

import com.grongo.cloud_storage_app.models.sharedItems.dto.SharedItemRequest;

public interface SharedItemsService {

    /**
     * Creates a shared item row in the database,
     * mapping a resource to a user by the given id
     * @param sharedItemRequest a dto containing the item, target email and role
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.ItemNotFoundException if item can't be found
     * @throws com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException if user can't be found
     * @throws com.grongo.cloud_storage_app.exceptions.sharedItemsException.TargetEmailConflictException if the email provided is the same as the resource's owner
     * @throws com.grongo.cloud_storage_app.exceptions.sharedItemsException.DuplicateSharedItemException if item has already been shared with target email
     */
    void createSharedItem(SharedItemRequest sharedItemRequest);

    /**
     * Fully updates a shared item instance in the database
     * @param sharedItemRequest a dto containing the item, target email and role
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.ItemNotFoundException if item can't be found
     * @throws com.grongo.cloud_storage_app.exceptions.sharedItemsException.ItemNotSharedException if item isn't shared at all, so nothing to update
     */
    void updateSharedItem(SharedItemRequest sharedItemRequest, Long id);

    /**
     * Deletes a shared item by an email and the itemId
     *
     */
    void deleteSharedItem(Long id);
}
