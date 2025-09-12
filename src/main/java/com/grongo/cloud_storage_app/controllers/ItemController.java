package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.models.items.dto.*;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.grongo.cloud_storage_app.services.items.impl.ItemService;
import com.grongo.cloud_storage_app.services.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final StorageService storageService;
    private final TagService tagService;
    private final ItemService itemService;

    @PatchMapping("/move/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleItemMove(
            @PathVariable Long itemId,
            @RequestBody MoveItemRequest moveItemRequest
            ){
        storageService.moveItem(itemId, moveItemRequest.getNewFolderId());
    }

    @PatchMapping("/rename/{itemId}")
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

    @PatchMapping("/{itemId}/tag/{tagId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void handleTagBinding(
            @PathVariable Long itemId,
            @PathVariable Long tagId
    ){
        tagService.bindTagToFile(tagId, itemId);
    }

    @DeleteMapping("/{itemId}/tag/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleTagUnbind(
            @PathVariable Long itemId,
            @PathVariable Long tagId
    ){
        tagService.unbindTagFromFile(tagId, itemId);
    }

    @GetMapping("/query")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> handleItemQuery(
            QueryItemDto queryItemDto
            ){
        return storageService.queryFiles(queryItemDto);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleItemsDelete(
            @RequestParam List<Long> itemId
    ){
        itemService.deleteItems(itemId);
    }
}
