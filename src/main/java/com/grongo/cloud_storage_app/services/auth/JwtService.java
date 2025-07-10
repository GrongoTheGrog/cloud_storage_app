package com.grongo.cloud_storage_app.services.auth;

import com.grongo.cloud_storage_app.models.token.dto.AccessTokenResponse;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;

import java.util.Optional;

public interface JwtService {
    AccessTokenResponse createAccessToken(Long userId, String userEmail);

    //Return the cookie;
    String createRefreshToken(Long userId, String userEmail, UserDto userDto);

    //Verifies a token and return its claims;
    Claims verifyToken(String token);

    //Creates a refresh token and creates a cookie for that
    Cookie getRefreshTokenCookie(Long userId, String userEmail, UserDto userDto);

    //Delete refresh token in the database
    void deleteRefreshToken(Cookie refreshCookie);

    //Find the refresh token in the database by its id
    Optional<String> findRefreshById(String id);

    //Create an empty refresh token cookie for logging out an user
    Cookie createEmptyRefreshIdCookie();
}
