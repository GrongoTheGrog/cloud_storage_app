package com.grongo.cloud_storage_app.models.items.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadFileForm {

    private Long folderId;
    @NotBlank
    private MultipartFile file;
    private String fileName;
    private Boolean isPublic;
}
