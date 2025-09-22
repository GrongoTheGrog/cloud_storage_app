package com.grongo.cloud_storage_app.unitTests;

import com.grongo.cloud_storage_app.aws.AwsService;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.dto.UploadFileForm;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FileRepository;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.services.FileTypeDetector;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.items.StorageService;
import com.grongo.cloud_storage_app.services.items.impl.FileServiceImpl;
import com.grongo.cloud_storage_app.services.items.impl.StorageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.parameters.P;

import java.nio.file.Path;

import static com.grongo.cloud_storage_app.TestUtils.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class FileTest {

    @Mock
    private AuthService authService;
    @Mock
    private StorageService storageService;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private AwsService awsService;

    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    public void testIfFileCanBeCreated() {
        User mockUser = new User();
        when(authService.getCurrentAuthenticatedUser()).thenReturn(mockUser);

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "test".getBytes()
        );

        doReturn("text/plain").when(awsService).uploadResourceFile(any(), any());
        fileService.createFile(mockMultipartFile, null, "newName", false);

        verify(storageService, times(1)).updatePath(any(File.class));

    }

}