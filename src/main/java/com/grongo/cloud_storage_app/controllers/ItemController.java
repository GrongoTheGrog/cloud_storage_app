package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.models.items.dto.ItemVisibilityUpdateRequest;
import com.grongo.cloud_storage_app.models.items.dto.MoveItemRequest;
import com.grongo.cloud_storage_app.models.items.dto.RenameItemRequest;
import com.grongo.cloud_storage_app.services.items.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller that holds the logic suitable for both files and folders
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final StorageService storageService;

    @PatchMapping("/move/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleItemMove(
            @PathVariable Long itemId,
            @RequestBody MoveItemRequest moveItemRequest
            ){
        storageService.moveItem(itemId, moveItemRequest.getNewFolderId());
    }

    @PatchMapping("/rename/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleItemRename(
            @PathVariable Long itemId,
            @RequestBody RenameItemRequest renameItemRequest
            ){
        storageService.renameItem(itemId, renameItemRequest.getNewName());
    }

    @PatchMapping("/visibility/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleItemVisibilityUpdate(
            @Validated @RequestBody ItemVisibilityUpdateRequest itemVisibilityUpdateRequest,
            @PathVariable Long itemId
            ){
        storageService.updateItemVisibility(itemVisibilityUpdateRequest, itemId);
    }


}
