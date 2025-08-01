package com.grongo.cloud_storage_app.services.auth.impl;

import com.grongo.cloud_storage_app.exceptions.userExceptions.UserNotFoundException;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.models.user.dto.AuthenticateUser;
import com.grongo.cloud_storage_app.models.user.dto.RegisterUser;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.security.CustomUserDetails;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthServiceImpl implements AuthService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final JwtServiceImpl jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserDto createUserCredentials(RegisterUser registerUser){
        User user = modelMapper.map(registerUser, User.class);

        if (user.getPassword() != null){
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);
        }

        userRepository.save(user);
        log.info("User {} created.", user.getId());
        return modelMapper.map(user, UserDto.class);
    }

    public UserDto authenticateUserCredentials(AuthenticateUser authenticateUser){
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authenticateUser.getEmail(),
                    authenticateUser.getPassword()
                )
        );

        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();

        log.info("User {} authenticated.", user.getId());
        return modelMapper.map(user, UserDto.class);
    }

    public Cookie logoutUser(Cookie refreshCookie){
        jwtService.deleteRefreshToken(refreshCookie);

        log.info("User logged out");

        return jwtService.createEmptyRefreshIdCookie();
    }

    @Override
    public User getCurrentAuthenticatedUser() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User authenticated does not exist."));
    }

}
