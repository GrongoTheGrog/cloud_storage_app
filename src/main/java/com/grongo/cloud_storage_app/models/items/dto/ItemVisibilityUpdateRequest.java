package com.grongo.cloud_storage_app.models.items.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemVisibilityUpdateRequest {
    @NotNull
    public Boolean isPublic;
}
