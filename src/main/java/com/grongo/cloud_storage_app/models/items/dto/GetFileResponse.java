package com.grongo.cloud_storage_app.models.items.dto;

import com.grongo.cloud_storage_app.services.sharedItems.FileRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class GetFileResponse {
    FileDto file;
    FileRole fileRole;
}
