package com.grongo.cloud_storage_app.aws;

import com.grongo.cloud_storage_app.exceptions.storageExceptions.StorageException;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.TempFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsService {

    private final S3Client s3;
    private final S3Presigner s3Presigner;

    @Value("${PROFILE_PICTURE_BUCKET_NAME}")
    private String PIC_BUCKET_NAME;

    @Value("${PROFILE_PICTURE_BUCKET_REGION}")
    private String PROFILE_PICTURE_BUCKET_REGION;

    public String postProfileImage(MultipartFile multipartFile, Long userId ){
        try{
            InputStream inputStream = multipartFile.getInputStream();
            Path tempFile = Files.createTempFile("profilePics" + userId, null);
            tempFile.toFile().delete();
            tempFile.toFile().deleteOnExit();


            Files.copy(inputStream, tempFile);

            PutObjectResponse putObjectResponse = s3.putObject(
                PutObjectRequest.builder()
                .bucket(PIC_BUCKET_NAME)
                .key(userId.toString())
                .contentType(multipartFile.getContentType())
                .build(),
                tempFile
            );

            log.info("Profile picture of user {} uploaded successfully.", userId);

            return formatS3Url(userId);

        }catch (IOException e){
            throw new StorageException("Error extracting input stream from multipart file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (Exception e){
            throw new StorageException("Error storing file in the server. Try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteProfilePic(Long userId){
        try{
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(PIC_BUCKET_NAME)
                    .key(userId.toString())
                    .build()
            );
        }catch (Exception e){
            throw new StorageException("Error deleting the profile picture. Try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public String getSignedUrlProfilePic(Long userId){
        try{
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(PIC_BUCKET_NAME)
                    .key(userId.toString())
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(
                    GetObjectPresignRequest.builder()
                            .getObjectRequest(getObjectRequest)
                            .signatureDuration(Duration.ofMinutes(60))
                            .build()
            );

            return presignedGetObjectRequest.url().toString();
        }catch (Exception e){
            throw new StorageException("Error retrieving profile image.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String formatS3Url(Long userId){
        return String.format("https://%s.s3.%s.amazonaws.com/%d", PIC_BUCKET_NAME, PROFILE_PICTURE_BUCKET_REGION, userId);
    }
}
