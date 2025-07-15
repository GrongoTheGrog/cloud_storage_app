package com.grongo.cloud_storage_app.services;


import com.grongo.cloud_storage_app.exceptions.storageExceptions.FileTypeException;
import com.rometools.utils.IO;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class FileTypeDetector {

    private static final Tika tika = new Tika();

    public static String getFileType(Path path) {
        try{
            return tika.detect(path);
        }catch (IOException e){
            throw new FileTypeException("Could not get file type.");
        }

    }

    public static String getExtension(String mimeType) {
        try{
            return MimeTypes.getDefaultMimeTypes().forName(mimeType).getExtension();
        }catch (MimeTypeException e){
            throw new FileTypeException("Could not get extension.");
        }

    }

}
