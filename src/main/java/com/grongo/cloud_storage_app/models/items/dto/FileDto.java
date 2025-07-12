package com.grongo.cloud_storage_app.models.items.dto;


import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDto {
    private Long id;
    private String name;
    private User owner;
    private Folder folder;
    private String path;
    private Long size;
    private String fileType;
}
