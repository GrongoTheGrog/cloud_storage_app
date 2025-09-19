package com.grongo.cloud_storage_app.models.tag.dtos;


import com.grongo.cloud_storage_app.models.tag.TagJoin;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagDto {

    private Long id;
    private String name;
    private String hex_color;
    private String description;
    private UserDto user;
}
