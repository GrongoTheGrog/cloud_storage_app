package com.grongo.cloud_storage_app.unitTests;


import com.grongo.cloud_storage_app.aws.AwsService;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FileRepository;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.ItemRepository;
import com.grongo.cloud_storage_app.services.FileTypeDetector;
import com.grongo.cloud_storage_app.services.auth.impl.AuthServiceImpl;
import com.grongo.cloud_storage_app.services.items.impl.FileServiceImpl;
import com.grongo.cloud_storage_app.services.items.impl.FolderServiceImpl;
import com.grongo.cloud_storage_app.services.items.impl.StorageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Optional;

import static com.grongo.cloud_storage_app.TestUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FolderTests {
    @Mock
    FolderRepository folderRepository;
    @Mock
    ItemRepository itemRepository;
    @Mock
    StorageServiceImpl storageService;
    @Mock
    AuthServiceImpl authService;
    @Mock
    FileRepository fileRepository;
    @Mock
    FileTypeDetector fileTypeDetector;
    @Mock
    ModelMapper modelMapper;
    @Mock
    AwsService awsService;

    @InjectMocks
    FolderServiceImpl folderService;
    @InjectMocks
    FileServiceImpl fileService;

    @Test
    public void testIfFolderSizeUpdatesWhenFileIsCreated() {
        User user = User.builder().id(0L).build();

        Folder folder = getFolder("folder", null, user);
        folder.setId(0L);

        FileServiceImpl spyService = spy(fileService);

        doReturn(user).when(authService).getCurrentAuthenticatedUser();


        doAnswer(invocation -> {
            folder.setSize(invocation.getArgument(1, Long.class));
            return null;
        }).when(storageService).updateSize(eq(folder), any(Long.class));

        doNothing().when(fileService).updateFile(any(), any());

        MockMultipartFile mockMultipartFile = new MockMultipartFile(
                "file",
                "file.txt",
                "text/plain",
                "test content".getBytes()
        );
        spyService.createFile(mockMultipartFile, folder.getId(), null, false);

        assertThat(folder.getSize()).isEqualTo(mockMultipartFile.getSize());
    }
}
