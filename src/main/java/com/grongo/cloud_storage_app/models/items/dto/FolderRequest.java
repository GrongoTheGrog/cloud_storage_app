package com.grongo.cloud_storage_app.models.items.dto;

import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderRequest {
    private String name;
    private Long parentFolderId;
    private Boolean isPublic;
}
