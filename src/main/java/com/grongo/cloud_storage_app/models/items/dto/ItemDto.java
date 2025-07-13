package com.grongo.cloud_storage_app.models.items.dto;

import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.user.User;
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
    private User owner;
    private Folder folder;
    private String path;
    private String type;
    private Date created_at;
    private Date updated_at;

}
