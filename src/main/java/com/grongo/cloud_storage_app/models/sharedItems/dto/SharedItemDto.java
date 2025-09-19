package com.grongo.cloud_storage_app.models.sharedItems.dto;

import com.grongo.cloud_storage_app.models.items.dto.ItemDto;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import com.grongo.cloud_storage_app.services.sharedItems.FileRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SharedItemDto {
    private Long id;
    private ItemDto item;
    private UserDto owner;
    private UserDto user;
    private FileRole fileRole;
}
