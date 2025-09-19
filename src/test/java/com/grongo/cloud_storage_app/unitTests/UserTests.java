package com.grongo.cloud_storage_app.unitTests;


import com.grongo.cloud_storage_app.aws.AwsService;
import com.grongo.cloud_storage_app.models.items.File;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import com.grongo.cloud_storage_app.repositories.*;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.user.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserTests {


    @Spy
    private ModelMapper modelMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthService authService;
    @Mock
    private AwsService awsService;
    @Mock
    private FileRepository fileRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private RefreshRepository refreshRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private SharedItemRepository sharedItemRepository;

    @InjectMocks
    UserServiceImpl userService;

    private final User user = User.builder()
            .id(1L)
            .email("TEST")
            .username("test")
            .build();

    @BeforeEach
    public void mocking(){
        when(authService.getCurrentAuthenticatedUser()).thenReturn(user);
    }

    @Test
    public void testPicturePosting(){
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(awsService.postProfileImage(any(MultipartFile.class), any(Long.class))).thenReturn("Link");

        MultipartFile multipartFile = new MockMultipartFile(
                "test",
                "test",
                "plain/text",
                "test".getBytes()
        );

        UserDto userDto = userService.changeUserPicture(user.getId(), multipartFile);

        assertThat(userDto.getPicture()).isEqualTo("Link");
        verify(awsService, times(1)).postProfileImage(any(), anyLong());
    }

    @Test
    public void testIfRenameLogicIsWorking(){

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        final String USERNAME = "newUsername";
        UserDto userDto = userService.updateUsername(user.getId(), USERNAME);

        assertThat(userDto.getUsername()).isEqualTo(USERNAME);
    }

    @Test
    public void testIfDeleteUserLogicIsWorking(){
        doNothing().when(awsService).deleteProfilePic(any());
        doNothing().when(awsService).deleteResourceFile(any());
        doReturn(List.of(
                File.builder().id(1L).build(),
                File.builder().id(2L).build())
        )
                .when(fileRepository)
                .findByUserId(user.getId());

        userService.deleteUser(user.getId());

        verify(awsService, times(2)).deleteResourceFile(any());
        verify(awsService, times(1)).deleteProfilePic(anyLong());
        verify(fileRepository, times(1)).findByUserId(any());
    }

}
