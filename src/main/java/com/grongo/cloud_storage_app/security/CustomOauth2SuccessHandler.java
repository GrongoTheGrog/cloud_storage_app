package com.grongo.cloud_storage_app.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.User;
import com.grongo.cloud_storage_app.models.dto.AccessTokenResponse;
import com.grongo.cloud_storage_app.models.dto.RequestUser;
import com.grongo.cloud_storage_app.models.dto.UserDto;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.auth.JwtService;
import com.grongo.cloud_storage_app.services.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizationSuccessHandler;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

//  That custom Oauth2 success handler lies at the end of the oauth login lifecycle.
//

@Component
@RequiredArgsConstructor
public class CustomOauth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final UserService userService;

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

        RequestUser requestUser = RequestUser.builder()
                .username(username)
                .email(email)
                .picture(picture)
                .build();

        UserDto userDto = userService.findByEmail(email).orElseGet(() -> authService.createUserCredentials(requestUser));

        AccessTokenResponse accessTokenResponse = jwtService.createAccessToken(userDto.getId(), email);
        Cookie refreshTokenSessionCookie = jwtService.getRefreshTokenCookie(userDto.getId(), email, userDto);

        response.addCookie(refreshTokenSessionCookie);

        response.setStatus(203);
        response.setContentType("application/json");
        response.getWriter().write(new ObjectMapper().writeValueAsString(accessTokenResponse));
    }
}

