package com.grongo.cloud_storage_app.unitTests;


import com.grongo.cloud_storage_app.aws.AwsService;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import com.grongo.cloud_storage_app.repositories.UserRepository;
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

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserTests {

    @Mock
    AwsService awsService;
    @Spy
    ModelMapper modelMapper;
    @Mock
    UserRepository userRepository;
    @Mock
    AuthService authService;

    @InjectMocks
    UserServiceImpl userService;

    private final User user = User.builder()
            .id(1L)
            .email("TEST")
            .username("test")
            .build();

    @BeforeEach
    public void mocking(){
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(authService.getCurrentAuthenticatedUser()).thenReturn(user);
    }

    @Test
    public void testPicturePosting(){
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

        final String USERNAME = "newUsername";
        UserDto userDto = userService.updateUsername(user.getId(), USERNAME);

        assertThat(userDto.getUsername()).isEqualTo(USERNAME);


    }

}
