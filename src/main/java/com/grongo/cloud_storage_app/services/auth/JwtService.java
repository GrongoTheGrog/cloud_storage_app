package com.grongo.cloud_storage_app.services.auth;

import com.grongo.cloud_storage_app.models.token.dto.AccessTokenResponse;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;

import java.util.Optional;

public interface JwtService {


    AccessTokenResponse createAccessToken(Long userId, String userEmail);

    String createRefreshToken(Long userId, String userEmail, UserDto userDto);

    Claims verifyToken(String token);

    Cookie getRefreshTokenCookie(Long userId, String userEmail, UserDto userDto);

    void deleteRefreshToken(Cookie refreshCookie);

    Optional<String> findRefreshById(String id);

    Cookie createEmptyRefreshIdCookie();

    Cookie getRefreshTokenFromCode(String code);
}
