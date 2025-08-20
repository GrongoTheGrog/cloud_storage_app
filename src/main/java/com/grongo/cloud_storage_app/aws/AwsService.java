package com.grongo.cloud_storage_app.aws;

import com.grongo.cloud_storage_app.exceptions.storageExceptions.StorageException;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import com.grongo.cloud_storage_app.services.FileTypeDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.TempFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsService {

    private final S3Client s3;
    private final S3Presigner s3Presigner;
    private final FileTypeDetector fileTypeDetector;

    @Value("${PROFILE_PICTURE_BUCKET_NAME}")
    private String PIC_BUCKET_NAME;

    @Value("${PROFILE_PICTURE_BUCKET_REGION}")
    private String PROFILE_PICTURE_BUCKET_REGION;

    @Value("${BUCKET_NAME}")
    private String STORAGE_BUCKET_NAME;

    private final Duration STORAGE_LINK_TTL = Duration.ofMinutes(10);

    public String postProfileImage(MultipartFile multipartFile, Long userId ){
        try{
            Path tempPath = getTempFile(multipartFile);

            PutObjectResponse putObjectResponse = s3.putObject(
                PutObjectRequest.builder()
                .bucket(PIC_BUCKET_NAME)
                .key(userId.toString())
                .contentType(multipartFile.getContentType())
                .build(),
                tempPath
            );

            log.info("Profile picture of user {} uploaded successfully.", userId);

            Files.deleteIfExists(tempPath);

            return formatS3Url(userId);

        } catch (S3Exception e){
            throw new StorageException("Error storing file in the server. Try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            throw new StorageException("Error deleting temporary file after uploading.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteProfilePic(Long userId){
        try{
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(PIC_BUCKET_NAME)
                    .key(userId.toString())
                    .build()
            );
        }catch (S3Exception e){
            throw new StorageException("Error deleting the profile picture. Try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public String uploadResourceFile(File file, MultipartFile multipartFile){
        try{
            Path tempPath = getTempFile(multipartFile);

            log.info("Uploading resource file.");

            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(STORAGE_BUCKET_NAME)
                            .key(file.getId().toString())
                            .contentType(file.getFileType())
                            .build(),
                    tempPath
            );

            log.info("Resource file uploaded.");

            String fileType = fileTypeDetector.getFileType(tempPath);
            Files.deleteIfExists(tempPath);
            return fileType;
        }catch (S3Exception e){
            throw new StorageException("Error uploading the file to the server. Try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            throw new StorageException("Error deleting temporary file after uploading file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String getStorageFileLink(Long fileId){
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(STORAGE_BUCKET_NAME)
                .key(fileId.toString())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(STORAGE_LINK_TTL)
                .getObjectRequest(getObjectRequest)
                .build();

        log.info("Deleting file {}...", fileId);

        PresignedGetObjectRequest request = s3Presigner.presignGetObject(presignRequest);

        log.info("File {} deleted.", fileId);
        return request.url().toString();
    }

    public void deleteResourceFile(Long fileId){
        try{
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(STORAGE_BUCKET_NAME)
                    .key(fileId.toString())
                    .build();

            log.info("Deleting file {}...", fileId);

            s3.deleteObject(request);

            log.info("File {} deleted.", fileId);
        }catch (S3Exception e){
            throw new StorageException("Error deleting file. Try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String formatS3Url(Long userId){
        return String.format("https://%s.s3.%s.amazonaws.com/%d", PIC_BUCKET_NAME, PROFILE_PICTURE_BUCKET_REGION, userId);
    }

    private Path getTempFile(MultipartFile multipartFile){
        try{
            InputStream inputStream = multipartFile.getInputStream();
            Path tempPath = Files.createTempFile("tempResourcePath", null);
            Files.deleteIfExists(tempPath);
            tempPath.toFile().deleteOnExit();

            Files.copy(inputStream, tempPath);
            return tempPath;
        }catch (IOException e){
            throw new StorageException("Error reading file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
