package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.exceptions.HttpException;
import com.grongo.cloud_storage_app.models.items.dto.*;
import com.grongo.cloud_storage_app.services.items.FolderService;
import com.grongo.cloud_storage_app.services.items.StorageService;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FolderDto handleFolderCreation(
            @RequestBody FolderRequest folderRequest
            ){
        return folderService.createFolder(folderRequest);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GetFolderResponse handleFindFolderById(
            @PathVariable String id
    ){
        try{
            Long folderId = id.equals("root") ? null : Long.parseLong(id);
            return folderService.findFolderById(folderId);
        }catch (NumberFormatException e){
            throw new HttpException("Enter either root or an id as the path variable.", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/open/{id}")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDto> handleFolderOpening(
            @PathVariable String id
    ){
        try{
            Long folderId = id.equals("root") ? null : Long.parseLong(id);
            return folderService.openFolder(folderId);
        }catch (NumberFormatException e){
            throw new HttpException("Enter either root or an id as the path variable.", HttpStatus.BAD_REQUEST);
        }
    }

}
