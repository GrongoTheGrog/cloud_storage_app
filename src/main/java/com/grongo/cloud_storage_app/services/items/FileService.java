package com.grongo.cloud_storage_app.services.items;

import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.dto.FileDto;
import com.grongo.cloud_storage_app.models.items.dto.UploadFileForm;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    void createFile(MultipartFile requestFile, Long folderId, String requestFileName, Boolean isPublic);
    String getSignedUrl(Long fileId);
    void deleteFile(Long fileId);
    void deleteFile(File file);
    void updateFile(UploadFileForm uploadFileForm, Long id);
}
