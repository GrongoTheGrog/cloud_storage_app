package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.services.items.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controller that holds the logic suitable for both files and folders
 */
@RestController
@RequestMapping("/api/items/move")
@RequiredArgsConstructor
public class ItemController {

    private final StorageService storageService;

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleItemMove(
            @PathVariable Long itemId,
            @RequestBody Long newFolderId
    ){
        storageService.moveItem(itemId, newFolderId);
    }


}
