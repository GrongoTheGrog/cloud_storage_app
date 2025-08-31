package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.models.tag.dtos.TagCreationDto;
import com.grongo.cloud_storage_app.services.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void handleTagCreation(
            @Validated @RequestBody TagCreationDto tagCreationDto
            ){
        tagService.createTag(tagCreationDto);
    }

    @DeleteMapping("/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleTagDeletion(
            @PathVariable("tagId") Long tagId
    ){
        tagService.deleteTag(tagId);
    }
}
