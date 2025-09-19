package com.grongo.cloud_storage_app.services.sharedItems;

import com.grongo.cloud_storage_app.models.sharedItems.dto.SharedItemDto;
import com.grongo.cloud_storage_app.models.sharedItems.dto.SharedItemRequest;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;

import java.util.List;

public interface SharedItemsService {

    SharedItemDto createSharedItem(SharedItemRequest sharedItemRequest);
    void updateSharedItem(SharedItemRequest sharedItemRequest, Long id);
    void deleteSharedItem(Long id);

    List<SharedItemDto> getSharingUsers(Long itemId);
}
