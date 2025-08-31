package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.aws.LinkTypes;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.dto.FileDto;
import com.grongo.cloud_storage_app.models.items.dto.UploadFileForm;
import com.grongo.cloud_storage_app.services.items.FileService;
import com.grongo.cloud_storage_app.services.items.impl.FileServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FileDto handleFileUpload(
            @ModelAttribute UploadFileForm uploadFileForm
            ){

        return fileService.createFile(
                uploadFileForm.getFile(),
                uploadFileForm.getFolderId(),
                uploadFileForm.getFileName(),
                uploadFileForm.getIsPublic()
        );
    }

    @GetMapping("/{fileId}")
    @ResponseStatus(HttpStatus.OK)
    public FileDto getFileMetadata(
            @PathVariable Long fileId
    ){
        return fileService.getFileById(fileId);
    }

    @GetMapping("/download/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String handleFileDownload(
            @PathVariable Long id
    ){
        return fileService.getSignedUrl(id, LinkTypes.DOWNLOAD);
    }

    @GetMapping("/preview/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String handleFilePreview(
            @PathVariable Long id
    ){
        return fileService.getSignedUrl(id, LinkTypes.PREVIEW);
    }


    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleUpdateFileUpload(
            @ModelAttribute UploadFileForm uploadFileForm,
            @PathVariable Long id
    ){
        fileService.updateFile(uploadFileForm, id);
    }

}
