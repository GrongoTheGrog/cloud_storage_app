package com.grongo.cloud_storage_app.services.tag;

import com.grongo.cloud_storage_app.models.tag.Tag;
import com.grongo.cloud_storage_app.models.tag.dtos.TagCreationDto;

public interface TagService {
    void createTag(TagCreationDto tagCreationDto);
    void deleteTag(Long tagId);
    void bindTagToFile(Long tagId, Long itemId);
    Tag findById(Long tagId);
    void unbindTagFromFile(Long tagId, Long itemId);
}
