package com.grongo.cloud_storage_app.models.items.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class MoveItemRequest {

    private Long newFolderId;

}
