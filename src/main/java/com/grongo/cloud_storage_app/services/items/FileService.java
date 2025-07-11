package com.grongo.cloud_storage_app.services.items;

import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    void createFile(MultipartFile file, Long folderId);
    void findFileByName(Long userId, String fileName);
}
