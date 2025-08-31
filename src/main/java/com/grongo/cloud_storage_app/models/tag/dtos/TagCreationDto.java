package com.grongo.cloud_storage_app.models.tag.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagCreationDto {

    @NotNull(message = "Name can't be null.")
    @NotBlank(message = "Name can't be empty.")
    private String name;
    @NotNull(message = "Color can't be null")
    @NotBlank(message = "Color can't be empty.")
    @Pattern(regexp = "^([0-9a-fA-F]{3,6})$")
    private String hex_color;
    @NotNull
    @NotBlank
    private String description;

}
