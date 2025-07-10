package com.grongo.cloud_storage_app.services.user.impl;


import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<UserDto> findUserByUsername(String username){
        Optional<User> foundUser = userRepository.findByUsername(username);
        return foundUser.map(user -> modelMapper.map(user, UserDto.class));
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> findUserById(Long id){
        Optional<User> foundUser = userRepository.findById(id);
        return foundUser.map(user -> modelMapper.map(user, UserDto.class));
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> findByEmail(String email){
        Optional<User> foundUser = userRepository.findByEmail(email);
        return foundUser.map(user -> modelMapper.map(user, UserDto.class));
    }

}
