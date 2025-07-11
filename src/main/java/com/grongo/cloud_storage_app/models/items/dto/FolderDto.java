package com.grongo.cloud_storage_app.models.items.dto;


import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderDto {
    private Long id;
    private String name;
    private FolderDto folder;
    private UserDto owner;
    private String path;
    private Date createdAt;
    private Date updatedAt;
}
