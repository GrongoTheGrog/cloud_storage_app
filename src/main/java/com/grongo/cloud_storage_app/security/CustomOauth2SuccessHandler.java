package com.grongo.cloud_storage_app.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.token.JwtRefresh;
import com.grongo.cloud_storage_app.models.user.dto.RegisterUser;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import com.grongo.cloud_storage_app.services.auth.impl.AuthServiceImpl;
import com.grongo.cloud_storage_app.services.cache.CacheKeys;
import com.grongo.cloud_storage_app.services.cache.impl.RefreshTokenCodeCache;
import com.grongo.cloud_storage_app.services.jwt.JwtAccessService;
import com.grongo.cloud_storage_app.services.jwt.JwtRefreshService;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

//  That custom Oauth2 success handler lies at the end of the oauth login lifecycle.
//

@Component
@RequiredArgsConstructor
public class CustomOauth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthServiceImpl authService;
    private final JwtAccessService jwtAccessService;
    private final JwtRefreshService jwtRefreshService;
    private final RefreshTokenCodeCache refreshTokenCodeCache;
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

        String refreshTokenId = jwtRefreshService.persistToDbAndReturnId(userDto);
        Cookie refreshTokenCookie = jwtRefreshService.cookie(refreshTokenId);
        response.addCookie(refreshTokenCookie);

        String accessToken = jwtAccessService.create(userDto.getId(), userDto.getEmail());

        //  A random short-lived code is generated so the user can retrieve
        //  with a request the actual refreshTokenId
        String randomCode = UUID.randomUUID().toString();
        refreshTokenCodeCache.setKey(CacheKeys.refreshTokenKey(randomCode), refreshTokenId, Duration.ofSeconds(60));

        response.setStatus(301);
        String redirectUrl = UriComponentsBuilder
                .fromUri(URI.create(frontendRedirectString))
                .queryParam("username", userDto.getUsername())
                .queryParam("id", userDto.getId().toString())
                .queryParam("email", userDto.getEmail())
                .queryParam("picture", userDto.getPicture())
                .queryParam("code", randomCode)
                .queryParam("accessToken", accessToken)
                .build()
                .toUriString();
        response.setHeader("Location", redirectUrl);
        response.setStatus(301);
    }
}

