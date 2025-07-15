package com.grongo.cloud_storage_app.services.user;

import com.grongo.cloud_storage_app.models.user.dto.UserDto;

import java.util.Optional;

public interface UserService{
    /**
     * Finds username by username
     * @param username
     * @return dto
     */
    public Optional<UserDto> findUserByUsername(String username);

    /**
     * Finds username by id
     * @param id
     * @return dto
     */
    public Optional<UserDto> findUserById(Long id);

    /**
     * Finds username by email
     * @param email
     * @return dto
     */
    public Optional<UserDto> findByEmail(String email);
}
