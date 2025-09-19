package com.grongo.cloud_storage_app.models.tag.dtos;

import com.grongo.cloud_storage_app.models.items.dto.ItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagJoinDto {
    Long id;
    TagDto tag;
}
