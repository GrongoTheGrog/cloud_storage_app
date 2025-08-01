package com.grongo.cloud_storage_app.models.items.dto;

import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.modelmapper.internal.bytebuddy.implementation.bind.annotation.Super;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ItemDto {

    private Long id;
    private String name;
    private UserDto owner;
    private FolderDto folder;
    private String path;
    private String type;
    private Date created_at;
    private Date updated_at;
    private Boolean isPublic;
    private Long size;

}
