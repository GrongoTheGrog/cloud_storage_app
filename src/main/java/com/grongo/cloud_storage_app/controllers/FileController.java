package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.models.items.dto.FileDto;
import com.grongo.cloud_storage_app.models.items.dto.UploadFileForm;
import com.grongo.cloud_storage_app.services.items.FileService;
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

    @GetMapping("/download/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String handleFileDownload(
            @PathVariable Long id
    ){
        return fileService.getSignedUrl(id);
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
