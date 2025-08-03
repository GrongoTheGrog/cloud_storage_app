package com.grongo.cloud_storage_app.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.token.dto.AccessTokenResponse;
import com.grongo.cloud_storage_app.models.user.dto.RegisterUser;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import com.grongo.cloud_storage_app.services.auth.impl.AuthServiceImpl;
import com.grongo.cloud_storage_app.services.auth.impl.JwtServiceImpl;
import com.grongo.cloud_storage_app.services.user.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

//  That custom Oauth2 success handler lies at the end of the oauth login lifecycle.
//

@Component
@RequiredArgsConstructor
public class CustomOauth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthServiceImpl authService;
    private final JwtServiceImpl jwtService;
    private final ModelMapper modelMapper;
    private final UserServiceImpl userService;

    @Value("${FRONTEND_REDIRECT_URI}")
    String frontendRedirectString;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User auth2User = (OAuth2User) authentication.getPrincipal();

        String username = auth2User.getAttribute("name");
        String email = auth2User.getAttribute("email");
        String picture = auth2User.getAttribute("picture");

        RegisterUser registerUser = RegisterUser.builder()
                .username(username)
                .email(email)
                .picture(picture)
                .build();

        UserDto userDto = userService.findByEmail(email).orElseGet(() -> authService.createUserCredentials(registerUser));

        AccessTokenResponse accessTokenResponse = jwtService.createAccessToken(userDto.getId(), email);
        Cookie refreshTokenSessionCookie = jwtService.getRefreshTokenCookie(userDto.getId(), email, userDto);

        response.addCookie(refreshTokenSessionCookie);

        response.setStatus(301);
        String redirectUrl = UriComponentsBuilder
                .fromUri(URI.create(frontendRedirectString))
                .queryParam("username", username)
                .queryParam("id", userDto.getId().toString())
                .queryParam("access_token", accessTokenResponse.getAccessToken())
                .queryParam("email", userDto.getEmail())
                .queryParam("picture", userDto.getPicture())
                .build()
                .toUriString();
        response.setHeader("Location", redirectUrl);
        response.setStatus(301);
    }
}

