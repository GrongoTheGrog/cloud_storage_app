package com.grongo.cloud_storage_app.controllers;

import com.grongo.cloud_storage_app.models.sharedItems.dto.SharedItemDto;
import com.grongo.cloud_storage_app.models.sharedItems.dto.SharedItemRequest;
import com.grongo.cloud_storage_app.services.sharedItems.SharedItemsService;
import com.grongo.cloud_storage_app.services.sharedItems.impl.SharedItemsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sharedItems")
@RequiredArgsConstructor
public class SharedItemsController {

    private final SharedItemsService sharedItemsService;

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public SharedItemDto handleItemSharing(
            @Validated @RequestBody SharedItemRequest sharedItemRequest
    ){
        return sharedItemsService.createSharedItem(sharedItemRequest);
    }

    @PutMapping(consumes = "application/json", path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleSharedItemUpdate(
            @Validated @RequestBody SharedItemRequest sharedItemRequest,
            @PathVariable Long id
    ){
        sharedItemsService.updateSharedItem(sharedItemRequest, id);
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleDeleteSharedItem(
           @PathVariable Long id
    ){
        sharedItemsService.deleteSharedItem(id);
    }
}
