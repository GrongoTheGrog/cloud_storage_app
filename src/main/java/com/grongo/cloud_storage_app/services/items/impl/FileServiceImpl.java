package com.grongo.cloud_storage_app.services.items.impl;

import com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException;
import com.grongo.cloud_storage_app.exceptions.storageExceptions.*;
import com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.dto.FileDto;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FileRepository;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.cache.impl.DownloadLinkCache;
import com.grongo.cloud_storage_app.services.items.FileService;
import com.grongo.cloud_storage_app.services.items.StorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

import static com.grongo.cloud_storage_app.services.FileTypeDetector.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${BUCKET_NAME}")
    private String bucketName;

    private Duration linkTTL = Duration.ofMinutes(10);

    private final S3Client s3Client;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final StorageService storageService;
    private final FileRepository fileRepository;
    private final ModelMapper modelMapper;
    private final S3Presigner s3Presigner;
    private final AuthService authService;
    private final DownloadLinkCache downloadLinkCache;

    @PostConstruct
    public void bucketCheck(){
        if (bucketName == null) throw new UndefinedBucketNameException("Undefined S3 bucket name.");
    }

    @Override
    @Transactional
    public FileDto createFile(MultipartFile requestFile, Long folderId, String requestFileName) {

        //writing the stream into disk now so I don't have to read that again later
        final Path streamPath;
        try {
            streamPath = Files.createTempFile("streamfile_", null);
            streamPath.toFile().deleteOnExit();

            InputStream inputStream = requestFile.getInputStream();
            Files.copy(inputStream, streamPath, StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e){
            throw new StorageException("Could not read file stream.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

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

        String fileType = getFileType(streamPath);

        File file = File.builder()
                .owner(user)
                .folder(folder)
                .name(fileName)
                .fileType(fileType)
                .size(requestFile.getSize())
                .build();

        fileRepository.save(file);

        storageService.updatePath(file);

        try{
            s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(file.getId().toString())
                        .contentType(fileType)
                        .build(),
                streamPath
            );

            Files.deleteIfExists(streamPath);

            return modelMapper.map(file, FileDto.class);

        } catch (S3Exception e){
            throw new AmazonException("Failed to upload file to S3 bucket.");
        } catch (IOException e){
            throw new StorageException("Failed to create a temp file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    @Transactional(readOnly = true)
    public String getSignedUrl(Long fileId) {

        User user = authService.getCurrentAuthenticatedUser();

        File file = fileRepository
                .findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("Could not find file with id of " + fileId));

        storageService.checkItemPermission(file, user);

        String cachedLink = downloadLinkCache.getKey(fileId.toString());
        if (cachedLink != null) {
            downloadLinkCache.refreshKey(fileId.toString(), linkTTL);
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
        downloadLinkCache.setKey(fileId.toString(), url, linkTTL);
        return url;
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId){

        File file = fileRepository
                .findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("Could not find file with id of " + fileId));

        deleteFile(file);

    }

    @Override
    @Transactional
    public void deleteFile(File file){
        User user = authService.getCurrentAuthenticatedUser();

        storageService.checkItemPermission(file, user);

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
}
