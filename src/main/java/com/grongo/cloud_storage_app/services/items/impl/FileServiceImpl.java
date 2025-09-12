package com.grongo.cloud_storage_app.services.items.impl;

import com.grongo.cloud_storage_app.aws.AwsService;
import com.grongo.cloud_storage_app.aws.LinkTypes;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.*;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.dto.FileDto;
import com.grongo.cloud_storage_app.models.items.dto.GetFileResponse;
import com.grongo.cloud_storage_app.models.items.dto.GetFolderResponse;
import com.grongo.cloud_storage_app.models.items.dto.UploadFileForm;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FileRepository;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.cache.impl.DownloadLinkCache;
import com.grongo.cloud_storage_app.services.items.FileService;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.grongo.cloud_storage_app.services.sharedItems.FilePermission;
import com.grongo.cloud_storage_app.services.sharedItems.FileRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.grongo.cloud_storage_app.services.cache.CacheKeys;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class FileServiceImpl implements FileService {

    private final FolderRepository folderRepository;
    private final StorageService storageService;
    private final FileRepository fileRepository;
    private final AuthService authService;
    private final DownloadLinkCache downloadLinkCache;
    private final ModelMapper modelMapper;
    private final AwsService awsService;
    private final FileChecker fileChecker;

    private final Duration duration = Duration.ofMinutes(10);


    @Override
    public FileDto createFile(MultipartFile requestFile, Long folderId, String requestFileName, Boolean isPublic) {
        User user = authService.getCurrentAuthenticatedUser();

        Folder folder = null;
        if (folderId != null){
            folder = folderRepository
                    .findById(folderId)
                    .orElseThrow(() -> new FolderNotFoundException("Could not find folder with id of " + folderId));
        }

        String fileName = requestFileName == null ? requestFile.getOriginalFilename() : requestFileName;

        if (storageService.checkNameConflict(folderId, user.getId(), fileName)){
            throw new ConflictStorageException("There is already a file named " + fileName + " in the given directory.");
        }

        File file = File.builder()
                .owner(user)
                .folder(folder)
                .name(fileName)
                .size(requestFile.getSize())
                .type("FILE")
                .isPublic(Boolean.TRUE.equals(isPublic))
                .build();

        fileRepository.save(file);

        String fileType = awsService.uploadResourceFile(file, requestFile);

        file.setFileType(fileType);

        storageService.updateSize(folder, file.getSize());
        storageService.updatePath(file);

        return modelMapper.map(file, FileDto.class);
    }

    @Override
    public GetFileResponse getFileById(Long fileId) {
        User authenticated = authService.getCurrentAuthenticatedUser();

        File file = fileRepository
                .findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("File not found."));

        FileRole fileRole = fileChecker.checkItemPermission(file, authenticated, FilePermission.VIEW);

        log.info("User {} requested metadata of file {}.", authenticated.getId(), file.getId());

        return new GetFileResponse(modelMapper.map(file, FileDto.class), fileRole);
    }


    @Override
    @Transactional(readOnly = true)
    public String getSignedUrl(Long fileId, LinkTypes type) {

        User user = authService.getCurrentAuthenticatedUser();

        File file = fileRepository
                .findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("Could not find file with id of " + fileId));

        fileChecker.checkItemPermission(file, user, FilePermission.VIEW);

        String cachedLink = downloadLinkCache.getKey(fileId.toString());
        if (cachedLink != null) {
            downloadLinkCache.refreshKey(CacheKeys.fileLinkKey(fileId), duration);
            return cachedLink;
        }

        String url = awsService.getStorageFileLink(file, type);
        downloadLinkCache.setKey(CacheKeys.fileLinkKey(fileId), url, duration);
        return url;
    }


    @Override
    @Transactional
    public void deleteFile(File file){
        User user = authService.getCurrentAuthenticatedUser();

        fileChecker.checkItemPermission(file, user, FilePermission.DELETE);
        storageService.updateSize(file.getFolder(), -file.getSize());

        log.info("Deleting file of id {}.", file.getId());
        fileRepository.deleteById(file.getId());

        awsService.deleteResourceFile(file.getId());
    }

    @Override
    public void updateFile(UploadFileForm uploadFileForm, Long id) {
        Optional<File> optionalFile = fileRepository.findById(id);

        if (optionalFile.isEmpty()){
            createFile(uploadFileForm.getFile(), uploadFileForm.getFolderId(), uploadFileForm.getFileName(), false);
            return;
        }

        User authenticatedUser = authService.getCurrentAuthenticatedUser();
        File file = optionalFile.get();

        fileChecker.checkItemPermission(file, authenticatedUser, FilePermission.UPDATE);

        String fileType = awsService.uploadResourceFile(file, uploadFileForm.getFile());
        String name = uploadFileForm.getFileName() != null ? uploadFileForm.getFileName() : file.getName();
        String previousName = file.getName();
        Long previousSize = file.getSize();


        file.setFileType(fileType);
        file.setName(name);
        file.setSize(uploadFileForm.getFile().getSize());
        file.setFileType(fileType);

        //only update the path if the file has changed the name
        if (!previousName.equals(file.getName())){
            storageService.updatePath(file);
        }else{
            fileRepository.save(file);
        }

        storageService.updateSize(file.getFolder(), file.getSize() - previousSize);
    }
}
