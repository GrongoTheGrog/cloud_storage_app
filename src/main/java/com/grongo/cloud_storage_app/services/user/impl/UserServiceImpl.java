package com.grongo.cloud_storage_app.services.user.impl;


import com.grongo.cloud_storage_app.aws.AwsService;
import com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException;
import com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final AwsService awsService;

    @Transactional(readOnly = true)
    public Optional<UserDto> findByEmail(String email){
        Optional<User> foundUser = userRepository.findByEmail(email);
        return foundUser.map(user -> modelMapper.map(user, UserDto.class));
    }

    @Override
    public UserDto changeUserPicture(Long userId, MultipartFile file) {
        User authenticatedUser = authService.getCurrentAuthenticatedUser();
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Could not find user to update."));

        if (!user.getId().equals(authenticatedUser.getId())){
            throw new AccessDeniedException("Not allowed to change image.");
        }

        log.info("Updating user {}'s picture.", user.getId());

        String fileLink = awsService.postProfileImage(file, userId);
        user.setPicture(fileLink);
        userRepository.save(user);

        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUsername(Long userId, String username) {
        User authenticatedUser = authService.getCurrentAuthenticatedUser();
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Could not find user,")
                );

        if (!user.getId().equals(authenticatedUser.getId())) {
            throw new AccessDeniedException("User doesn't have permission to change username.");
        }

        log.info("Changing name for user {}.", userId);

        user.setUsername(username);
        userRepository.save(user);

        return modelMapper.map(user, UserDto.class);
    }
}
