package com.grongo.cloud_storage_app.services.items;

import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.dto.FileDto;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * Uploads the file to S3 and store metadata in database:
     *
     * @param requestFile multipart file
     * @param folderId the parent folder id (null if in root)
     * @param requestFileName the file name (if not provided, the original file name will be used)
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.AmazonException if an error occur while uploading to bucket
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.ConflictStorageException name conflict
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.FolderNotFoundException if no folder can be found with given folder id
     * @throws com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException if authenticated user can't be found
     * @return A FileDto object containing the file metadata stored in database
     */
    FileDto createFile(MultipartFile requestFile, Long folderId, String requestFileName);


    /**
     * Generates a presigned link of the given S3 resource with a duration of 10 minutes
     *
     *
     * @param fileId the id to fetch the file in database
     * @throws com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException if authenticated user can't be found
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.FileNotFoundException if file can't be found with given id
     * @throws com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException if authenticated used is not the owner of the resource
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.AmazonException if link generation fails
     * @return The download link for the S3 object
     */
    String getSignedUrl(Long fileId);


    /**
     * Deletes a files from the database and from S3 bucket
     *
     * @param fileId the id to delete the file
     * @throws com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException if authenticated user can't be found
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.FileNotFoundException if file can't be found with given id
     * @throws com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException if authenticated used is not the owner of the resource
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.AmazonException if object delete fails
     */
    void deleteFile(Long fileId);


    /**
     * Deletes a files from the database and from S3 bucket
     *
     * @param file the file to delete
     * @throws com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException if authenticated user can't be found
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.FileNotFoundException if file can't be found with given id
     * @throws com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException if authenticated used is not the owner of the resource
     * @throws com.grongo.cloud_storage_app.exceptions.storageExceptions.AmazonException if object delete fails
     */
    void deleteFile(File file);

}
