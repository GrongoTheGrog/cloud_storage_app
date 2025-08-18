package com.grongo.cloud_storage_app.unitTests;

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
    private FileTypeDetector fileTypeDetector;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private FileServiceImpl fileService;

    @Test
    public void testIfFileCanBeCreated() {
        // Arrange
        User mockUser = new User();
        when(authService.getCurrentAuthenticatedUser()).thenReturn(mockUser);

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "test".getBytes()
        );

        Path mockPath = Path.of("temp/file.txt");
        FileServiceImpl spyService = spy(fileService);
        doReturn(mockPath).when(spyService).getTempPathFromFile(mockMultipartFile);
        doReturn("text/plain").when(fileTypeDetector).getFileType(mockPath);

        when(storageService.checkNameConflict(null, mockUser.getId(), "file.txt")).thenReturn(false);
        doNothing().when(spyService).uploadFile((Path) any(), (File) any());

        spyService.createFile(mockMultipartFile, null, null, false);

        verify(fileRepository, times(1)).save(any(File.class));
        verify(storageService, times(1)).updateSize(null, mockMultipartFile.getSize());
        verify(storageService, times(1)).updatePath(any(File.class));
        verify(spyService, times(1)).uploadFile(any(Path.class), any(File.class));
    }


    @Test
    public void testIfFileCanBeUpdated(){
        User mockUser = new User();
        when(authService.getCurrentAuthenticatedUser()).thenReturn(mockUser);

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "test".getBytes()
        );

        Path mockPath = Path.of("temp/file.txt");
        FileServiceImpl spyService = spy(fileService);
        doReturn(mockPath).when(spyService).getTempPathFromFile(mockMultipartFile);
        doReturn("text/plain").when(fileTypeDetector).getFileType(mockPath);

        when(storageService.checkNameConflict(null, mockUser.getId(), "file.txt")).thenReturn(false);
        doNothing().when(spyService).uploadFile((Path) any(), (File) any());

        UploadFileForm uploadFileForm = UploadFileForm.builder()
                        .file(mockMultipartFile)
                        .build();

        spyService.updateFile(uploadFileForm, 0L);

        verify(fileRepository, times(1)).save(any(File.class));
        verify(storageService, times(1)).updateSize(null, mockMultipartFile.getSize());
        verify(storageService, times(1)).updatePath(any(File.class));
        verify(spyService, times(1)).uploadFile(any(Path.class), any(File.class));
    }
}