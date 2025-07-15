package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.models.items.dto.FileDto;
import com.grongo.cloud_storage_app.models.items.dto.UploadFileForm;
import com.grongo.cloud_storage_app.services.items.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String handleFileUpload(
            @ModelAttribute UploadFileForm uploadFileForm
            ){

        FileDto fileDto = fileService.createFile(
                uploadFileForm.getFile(),
                uploadFileForm.getFolderId(),
                uploadFileForm.getFileName()
        );

        return String.format("File %s uploaded on path %s.", fileDto.getName(), fileDto.getPath());
    }

    @GetMapping("/download/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String handleFileDownload(
            @PathVariable Long id
    ){
        return fileService.getSignedUrl(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleFileDeletion(
            @PathVariable Long id
    ){
        fileService.deleteFile(id);
    }

}
