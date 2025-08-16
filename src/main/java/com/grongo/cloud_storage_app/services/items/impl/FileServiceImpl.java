package com.grongo.cloud_storage_app.services.items.impl;

import com.grongo.cloud_storage_app.exceptions.storageExceptions.*;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.FileDto;
import com.grongo.cloud_storage_app.models.items.dto.UploadFileForm;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FileRepository;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.FileTypeDetector;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.cache.impl.DownloadLinkCache;
import com.grongo.cloud_storage_app.services.items.FileService;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.grongo.cloud_storage_app.services.sharedItems.FilePermission;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import com.grongo.cloud_storage_app.services.cache.CacheKeys;
import static com.grongo.cloud_storage_app.services.FileTypeDetector.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class FileServiceImpl implements FileService {

    @Value("${BUCKET_NAME}")
    private String bucketName;

    private final Duration linkTTL = Duration.ofMinutes(10);

    private final S3Client s3Client;
    private final FolderRepository folderRepository;
    private final StorageService storageService;
    private final FileRepository fileRepository;
    private final S3Presigner s3Presigner;
    private final AuthService authService;
    private final DownloadLinkCache downloadLinkCache;
    private final FileTypeDetector fileTypeDetector;
    private final ModelMapper modelMapper;

    @PostConstruct
    public void bucketCheck(){
        if (bucketName == null) throw new UndefinedBucketNameException("Undefined S3 bucket name.");
    }

    @Override
    public FileDto createFile(MultipartFile requestFile, Long folderId, String requestFileName, Boolean isPublic) {

        final Path streamPath = getTempPathFromFile(requestFile);

        User user = authService.getCurrentAuthenticatedUser();

        Folder folder = null;
        if (folderId != null){
            folder = folderRepository
                    .findById(folderId)
                    .orElseThrow(() -> new FolderNotFoundException("Could not find folder with id of " + folderId));
        }


        String fileName = requestFileName == null ? requestFile.getOriginalFilename() : requestFileName;

        String fileType = fileTypeDetector.getFileType(streamPath);

        if (storageService.checkNameConflict(folderId, user.getId(), fileName)){
            throw new ConflictStorageException("There is already a file named " + fileName + " in the given directory.");
        }

        File file = File.builder()
                .owner(user)
                .folder(folder)
                .name(fileName)
                .size(requestFile.getSize())
                .fileType(fileType)
                .type("FILE")
                .isPublic(Boolean.TRUE.equals(isPublic))
                .build();

        storageService.updateSize(folder, file.getSize());

        fileRepository.save(file);

        storageService.updatePath(file);

        uploadFile(streamPath, file);

        return modelMapper.map(file, FileDto.class);
    }


    @Override
    @Transactional(readOnly = true)
    public String getSignedUrl(Long fileId) {

        User user = authService.getCurrentAuthenticatedUser();

        File file = fileRepository
                .findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("Could not find file with id of " + fileId));

        storageService.checkItemPermission(file, user, FilePermission.VIEW);

        String cachedLink = downloadLinkCache.getKey(fileId.toString());
        if (cachedLink != null) {
            downloadLinkCache.refreshKey(CacheKeys.fileLinkKey(fileId), linkTTL);
            return cachedLink;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileId.toString())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(linkTTL)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);
        String url = presignedGetObjectRequest.url().toExternalForm();
        downloadLinkCache.setKey(CacheKeys.fileLinkKey(fileId), url, linkTTL);
        return url;
    }


    @Override
    @Transactional
    public void deleteFile(File file){
        User user = authService.getCurrentAuthenticatedUser();

        storageService.checkItemPermission(file, user, FilePermission.DELETE);
        storageService.updateSize(file.getFolder(), -file.getSize());


        log.info("Deleting file of id {}.", file.getId());
        fileRepository.deleteById(file.getId());

        try{
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .key(file.getId().toString())
                    .bucket(bucketName)
                    .build()
            );
        }catch (S3Exception e){
            throw new AmazonException("Could not delete file on S3 bucket.");
        }
    }

    @Override
    public void updateFile(UploadFileForm uploadFileForm, Long id) {
        Optional<File> optionalFile = fileRepository.findById(id);

        if (optionalFile.isEmpty()){
            createFile(uploadFileForm.getFile(), uploadFileForm.getFolderId(), uploadFileForm.getFileName(), false);
            return;
        }

        Path tempPath = getTempPathFromFile(uploadFileForm.getFile());

        User authenticatedUser = authService.getCurrentAuthenticatedUser();
        File file = optionalFile.get();

        storageService.checkItemPermission(file, authenticatedUser, FilePermission.UPDATE);

        String name = uploadFileForm.getFileName() != null ? uploadFileForm.getFileName() : file.getName();
        String fileType = fileTypeDetector.getFileType(tempPath);
        String previousName = file.getName();
        Long previousSize = file.getSize();


        file.setFileType(fileType);
        file.setName(name);
        file.setSize(uploadFileForm.getFile().getSize());

        //only update the path if the file has changed the name
        if (!previousName.equals(file.getName())){
            storageService.updatePath(file);
        }else{
            fileRepository.save(file);
        }

        storageService.updateSize(file.getFolder(), file.getSize() - previousSize);

        uploadFile(tempPath, file);

    }

    public void uploadFile(Path tempPath, File file){
        try{
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(file.getId().toString())
                            .contentType(file.getFileType())
                            .build(),
                    tempPath
            );

            Files.deleteIfExists(tempPath);

        } catch (S3Exception e){
            throw new AmazonException("Failed to upload file to S3 bucket.");
        } catch (IOException e){
            throw new StorageException("Failed to create a temp file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Path getTempPathFromFile(MultipartFile multipartFile){
        try {
            Path streamPath = Files.createTempFile("streamfile_", null);
            streamPath.toFile().deleteOnExit();

            InputStream inputStream = multipartFile.getInputStream();
            Files.copy(inputStream, streamPath, StandardCopyOption.REPLACE_EXISTING);
            return streamPath;
        }catch (IOException e){
            throw new StorageException("Could not read file stream.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
