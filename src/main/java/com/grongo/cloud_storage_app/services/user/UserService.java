package com.grongo.cloud_storage_app.services.user;

import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService{
    public Optional<UserDto> findByEmail(String email);
    public UserDto changeUserPicture(Long userId, MultipartFile picture);

    UserDto updateUsername(Long userId, String username);
}
