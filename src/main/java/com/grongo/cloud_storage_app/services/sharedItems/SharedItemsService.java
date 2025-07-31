package com.grongo.cloud_storage_app.services.sharedItems;

import com.grongo.cloud_storage_app.models.sharedItems.dto.SharedItemRequest;

public interface SharedItemsService {

    void createSharedItem(SharedItemRequest sharedItemRequest);
    void updateSharedItem(SharedItemRequest sharedItemRequest, Long id);
    void deleteSharedItem(Long id);
}
