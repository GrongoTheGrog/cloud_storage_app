package com.grongo.cloud_storage_app.services.user;


import com.grongo.cloud_storage_app.models.User;
import com.grongo.cloud_storage_app.models.dto.UserDto;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    public Optional<UserDto> findUserByUsername(String username){
        Optional<User> foundUser = userRepository.findByUsername(username);
        return foundUser.map(user -> modelMapper.map(user, UserDto.class));
    }

    public Optional<UserDto> findUserById(Long id){
        Optional<User> foundUser = userRepository.findById(id);
        return foundUser.map(user -> modelMapper.map(user, UserDto.class));
    }

    public Optional<UserDto> findByEmail(String email){
        Optional<User> foundUser = userRepository.findByEmail(email);
        return foundUser.map(user -> modelMapper.map(user, UserDto.class));
    }

}
