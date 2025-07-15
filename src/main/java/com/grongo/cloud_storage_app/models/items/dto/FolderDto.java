package com.grongo.cloud_storage_app.models.items.dto;


import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.modelmapper.internal.bytebuddy.implementation.bind.annotation.Super;

import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FolderDto extends ItemDto{
    private Long size;
}
