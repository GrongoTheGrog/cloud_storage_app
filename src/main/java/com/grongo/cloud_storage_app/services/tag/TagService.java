package com.grongo.cloud_storage_app.services.tag;

import com.grongo.cloud_storage_app.models.tag.Tag;
import com.grongo.cloud_storage_app.models.tag.dtos.TagCreationDto;
import com.grongo.cloud_storage_app.models.tag.dtos.TagDto;

import java.util.List;

public interface TagService {
    TagDto createTag(TagCreationDto tagCreationDto);
    void deleteTag(Long tagId);
    void bindTagToFile(Long tagId, Long itemId);
    Tag findById(Long tagId);
    void unbindTagFromFile(Long tagId, Long itemId);
    List<TagDto> getTags();
}
