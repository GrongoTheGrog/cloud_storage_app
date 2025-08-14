package com.grongo.cloud_storage_app.models.items.dto;



import lombok.*;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FolderDto extends ItemDto{
    private Long size;
}
