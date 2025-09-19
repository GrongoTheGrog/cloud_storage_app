package com.grongo.cloud_storage_app.services.items;

import com.grongo.cloud_storage_app.aws.LinkTypes;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.dto.FileDto;
import com.grongo.cloud_storage_app.models.items.dto.GetFileResponse;
import com.grongo.cloud_storage_app.models.items.dto.GetFolderResponse;
import com.grongo.cloud_storage_app.models.items.dto.UploadFileForm;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    FileDto createFile(MultipartFile requestFile, Long folderId, String requestFileName, Boolean isPublic);
    GetFileResponse getFileById(Long fileId);

    String getSignedUrl(Long fileId, LinkTypes type);

    void deleteFile(File file);
    void updateFile(UploadFileForm uploadFileForm, Long id);
}
