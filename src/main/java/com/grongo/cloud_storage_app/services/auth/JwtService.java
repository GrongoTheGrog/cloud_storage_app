package com.grongo.cloud_storage_app.services.auth;

import com.grongo.cloud_storage_app.models.token.dto.AccessTokenResponse;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;

import java.util.Optional;

public interface JwtService {

    /**
     * Creates an access token
     * @param userId the id to store in the token
     * @param userEmail the email to store in the token
     * @return a response format to return in the controller
     */
    AccessTokenResponse createAccessToken(Long userId, String userEmail);

    /**
     * creates the refresh token and assign that to database
     * @param userId the id to put in the token
     * @param userEmail the email to put in the token
     * @param userDto the userDto to assign to refreshToken in the database
     * @return the actual token
     */
    String createRefreshToken(Long userId, String userEmail, UserDto userDto);

    /**
     * verifies the token
     * @param token the token to be verified
     * @return the token's claims
     * @throws com.grongo.cloud_storage_app.exceptions.tokenExceptions.InvalidTokenException if token is not valid
     */
    Claims verifyToken(String token);

    /**
     * generates a refresh token, persists to the database and creates a
     * cookie with the refresh token id for subsequent requests
     * @param userId the id to put in the token
     * @param userEmail the email to put in the token
     * @param userDto the userDto to assign to refreshToken
     * @return the cookie containing the token
     */
    Cookie getRefreshTokenCookie(Long userId, String userEmail, UserDto userDto);

    /**
     * deletes the refreshToken from database and invalidates the cookie
     * @param refreshCookie to invalidate the cookie
     */
    void deleteRefreshToken(Cookie refreshCookie);

    /**
     * finds refresh token by id
     * @param id the id to look for
     * @return the token
     */
    Optional<String> findRefreshById(String id);

    /**
     * creates an empty refresh token cookie to invalidate the cookie
     * @return the empty cookie
     */
    Cookie createEmptyRefreshIdCookie();
}
