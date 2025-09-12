package com.grongo.cloud_storage_app.models.items.dto;

import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.sharedItems.SharedItem;
import com.grongo.cloud_storage_app.models.sharedItems.dto.SharedItemDto;
import com.grongo.cloud_storage_app.models.tag.TagJoin;
import com.grongo.cloud_storage_app.models.tag.dtos.TagJoinDto;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.HashCodeExclude;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.modelmapper.internal.bytebuddy.implementation.bind.annotation.Super;

import java.util.Date;
import java.util.List;
import java.util.Set;


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
    private Long size = 0L;
    @ToStringExclude
    @HashCodeExclude
    private List<TagJoinDto> tagJoins;
}
