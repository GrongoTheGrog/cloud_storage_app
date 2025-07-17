package com.grongo.cloud_storage_app.controllers;

import com.grongo.cloud_storage_app.models.sharedItems.dto.SharedItemRequest;
import com.grongo.cloud_storage_app.services.sharedItems.SharedItemsService;
import com.grongo.cloud_storage_app.services.sharedItems.impl.SharedItemsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items/share")
@RequiredArgsConstructor
public class SharedItemsController {

    private final SharedItemsService sharedItemsService;

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleItemSharing(
            @Validated @RequestBody SharedItemRequest sharedItemRequest
    ){
        sharedItemsService.sharedItem(sharedItemRequest);
    }
}
