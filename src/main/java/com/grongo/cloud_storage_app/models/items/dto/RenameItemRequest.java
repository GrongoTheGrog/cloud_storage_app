package com.grongo.cloud_storage_app.models.items.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RenameItemRequest {

    @NotNull
    @NotBlank
    private String newName;

}


