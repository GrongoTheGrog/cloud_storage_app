package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.exceptions.storageExceptions.FolderNotFoundException;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.FolderDto;
import com.grongo.cloud_storage_app.models.items.dto.FolderRequest;
import com.grongo.cloud_storage_app.models.items.dto.ItemDto;
import com.grongo.cloud_storage_app.services.items.FolderService;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.grongo.cloud_storage_app.services.user.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final StorageService storageService;

    @PostMapping(consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public String handleFolderCreation(
            @RequestBody FolderRequest folderRequest
            ){
        FolderDto folderDto = folderService.createFolder(folderRequest);

        return "Folder " + folderDto.getName() + " created successfully.";
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public FolderDto handleFindFolderById(
            @PathVariable Long id
    ){
        return folderService
                .findFolderById(id)
                .orElseThrow(() -> new FolderNotFoundException("Could not find folder with id of " + id));
    }

    @GetMapping("/open/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> handleFolderOpening(
            @PathVariable Long id
    ){
        return folderService.openFolder(id);
    }
}
