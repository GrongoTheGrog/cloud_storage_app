package com.grongo.cloud_storage_app.models.items.dto;


import lombok.*;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FileDto extends ItemDto{
    private String fileType;
}
