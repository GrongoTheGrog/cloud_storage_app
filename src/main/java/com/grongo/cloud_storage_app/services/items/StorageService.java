package com.grongo.cloud_storage_app.services.items;

import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.services.sharedItems.FilePermissions;

import java.util.List;

/**
 * Class intended for operations that suit both files and folders
 *
 * @author grongo
 */
public interface StorageService {

    /**
     * Updates the path of a given item an all its children if it's a folder
     *
     * @param itemId the id of the item to be updated
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.ItemNotFoundException if item can't be found with given id
     *
     */
    void updatePath(Long itemId);

    /** Updates the path of a given item an all its children if it's a folder
     *
     * @param item the item to be updated
     */
    void updatePath(Item item);

    /**
     * Changes the location of an item and call update path
     *
     * @param itemId the id of the item to be moved
     * @param newParentId the id of the folder to receive the item
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.ItemNotFoundException if no item can be found with given id
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.FolderNotFoundException if no folder can be found with given id
     * @throws com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException if authenticated user doesn't have the permission the move the file
     */
    void moveItem(Long itemId, Long newParentId);

    /**
     * Renames the file and uploads it's path
     *
     * @param itemId the id of the item to be renamed
     * @param name the name that the item will be renamed with
     * @throws com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException if authenticated user doesn't have the permission to rename item
     */
    void renameItem(Long itemId, String name);

    /**Get all the items inside a given folder
     *
     *
     * @param folderId folder to get the items from
     * @param userId used in case folderId is null and items are in root of user
     * @return A list of items
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.FolderNotFoundException could not find folder with given id
     */
    List<Item> getItemsInFolder(Long folderId, Long userId);

    /**Checks if a name already exists on a given directory
     *
     * @param folderId the id of the folder to be checked
     * @param userId used in case folderId is null and items are in root of user
     * @param itemName the name to be checked
     * @return a boolean representing whether there is a conflict (true) or not (false)
     */
    boolean checkNameConflict(Long folderId, Long userId, String itemName);

    /**
     * checks if the first folder is the ancestor of second folder
     * @param ancestor
     * @param child
     * @return
     */
    boolean checkIfFolderIsAncestor(Folder ancestor, Folder child);

    /**
     * Takes an item and a user, then throw if user is not the owner
     * @param item the item to check
     * @param user the user to check
     * @param filePermission the permission to check
     * @throws com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException if user is not the owner
     */
    void checkItemPermission(Item item, User user, FilePermissions filePermission);
}
