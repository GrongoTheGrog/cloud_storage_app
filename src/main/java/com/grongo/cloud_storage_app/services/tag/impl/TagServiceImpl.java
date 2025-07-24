package com.grongo.cloud_storage_app.services.tag.impl;

import com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.ItemNotFoundException;
import com.grongo.cloud_storage_app.exceptions.tags.TagConflictException;
import com.grongo.cloud_storage_app.exceptions.tags.TagNotFoundException;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.tag.Tag;
import com.grongo.cloud_storage_app.models.tag.TagJoin;
import com.grongo.cloud_storage_app.models.tag.dtos.TagCreationDto;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.ItemRepository;
import com.grongo.cloud_storage_app.repositories.JoinTagRepository;
import com.grongo.cloud_storage_app.repositories.TagRepository;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.grongo.cloud_storage_app.services.sharedItems.FilePermission;
import com.grongo.cloud_storage_app.services.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final AuthService authService;
    private final ItemRepository itemRepository;
    private final JoinTagRepository joinTagRepository;
    private final StorageService storageService;

    @Override
    public void createTag(TagCreationDto tagCreationDto) {
        User authenticatedUser = authService.getCurrentAuthenticatedUser();
        List<Tag> tagList = tagRepository.findByNameAndUserId(tagCreationDto.getName(), authenticatedUser.getId());
        if (!tagList.isEmpty()) throw new TagConflictException("User already has a tag named " + tagCreationDto.getName());

        Tag tag = Tag.builder()
                .name(tagCreationDto.getName())
                .hex_color(tagCreationDto.getHex_color())
                .build();

        tagRepository.save(tag);
    }

    @Override
    public void deleteTag(Long id){
        User authenticatedUser = authService.getCurrentAuthenticatedUser();
        Optional<Tag> tag = tagRepository.findById(id);
        if (tag.isPresent()){
            if (!tag.get().getUser().getId().equals(authenticatedUser.getId())){
                throw new AccessDeniedException("The tag is not yours to delete.");
            }
            tagRepository.delete(tag.get());
        }

    }

    @Override
    public void bindTagToFile(Long tagId, Long itemId) {
        Tag tag = findById(tagId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Could not find item with id of " + itemId));

        Optional<TagJoin> foundTagJoin = joinTagRepository.findByItemIdAndTagId(itemId, tagId);
        if (foundTagJoin.isPresent()) throw new TagConflictException("The tag '" + tag.getName() + "' is already bound to the item " + item.getPath());

        TagJoin tagJoin = TagJoin.builder()
                .tag(tag)
                .item(item)
                .build();

        joinTagRepository.save(tagJoin);

    }


    @Override
    public void unbindTagFromFile(Long tagId, Long itemId) {
        User authenticatedUser = authService.getCurrentAuthenticatedUser();
        Tag tag = findById(tagId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ItemNotFoundException("Could not find item with id of " + itemId));

        storageService.checkItemPermission(item, authenticatedUser, FilePermission.UPDATE);

        joinTagRepository.findByItemIdAndTagId(itemId, tagId)
                .map(tagJoin -> {
                    joinTagRepository.delete(tagJoin);
                    return tagJoin;
                });
    }

    @Override
    public Tag findById(Long tagId){
        return tagRepository.findById(tagId).orElseThrow(() -> new TagNotFoundException("Could not find tag of id " + tagId));
    }

}
