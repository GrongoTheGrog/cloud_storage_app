package com.grongo.cloud_storage_app.services.user;

import com.grongo.cloud_storage_app.models.user.dto.UserDto;

import java.util.Optional;

public interface UserService{
    public Optional<UserDto> findUserByUsername(String username);
    public Optional<UserDto> findUserById(Long id);
    public Optional<UserDto> findByEmail(String email);
}
