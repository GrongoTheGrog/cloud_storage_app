package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.exceptions.tokenExceptions.TokenException;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.TokenNotFoundException;
import com.grongo.cloud_storage_app.models.token.dto.AccessTokenResponse;
import com.grongo.cloud_storage_app.models.user.dto.AuthenticateUser;
import com.grongo.cloud_storage_app.models.user.dto.AuthenticateUserResponse;
import com.grongo.cloud_storage_app.models.user.dto.RegisterUser;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import com.grongo.cloud_storage_app.services.auth.impl.AuthServiceImpl;
import com.grongo.cloud_storage_app.services.auth.impl.JwtServiceImpl;
import com.grongo.cloud_storage_app.services.user.impl.UserServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;
    private final UserServiceImpl userService;
    private final JwtServiceImpl jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AuthenticateUserResponse> signup(
            @Validated @RequestBody RegisterUser registerUser,
            HttpServletResponse response
    ){
        UserDto userDto = authService.createUserCredentials(registerUser);

        AccessTokenResponse accessTokenResponse = jwtService.createAccessToken(userDto.getId(), userDto.getEmail());
        Cookie cookie = jwtService.getRefreshTokenCookie(userDto.getId(), userDto.getEmail(), userDto);
        response.addCookie(cookie);
        AuthenticateUserResponse userResponse = AuthenticateUserResponse.builder()
                .accessToken(accessTokenResponse.getAccessToken())
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .id(userDto.getId())
                .picture(userDto.getPicture())
                .build();


        return ResponseEntity.ok().body(userResponse);
    }


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AuthenticateUserResponse> login(
            @RequestBody AuthenticateUser authenticateUser,
            @CookieValue(name = "rt_session_id", required = false) Cookie refreshTokenCookieRequest,
            HttpServletRequest request,
            HttpServletResponse response
            ){
        UserDto userDto = authService.authenticateUserCredentials(authenticateUser);

        if (refreshTokenCookieRequest != null){
            jwtService.deleteRefreshToken(refreshTokenCookieRequest);
        }


        AccessTokenResponse accessToken = jwtService.createAccessToken(userDto.getId(), userDto.getEmail());
        Cookie refreshTokenCookie = jwtService.getRefreshTokenCookie(userDto.getId(), userDto.getEmail(), userDto);

        response.addCookie(refreshTokenCookie);

        AuthenticateUserResponse userResponse = AuthenticateUserResponse.builder()
                .username(userDto.getUsername())
                .id(userDto.getId())
                .email(userDto.getEmail())
                .picture(userDto.getPicture())
                .accessToken(accessToken.getAccessToken())
                .build();

        return ResponseEntity.ok().body(userResponse);

    }


    @GetMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AccessTokenResponse> refresh(
            HttpServletResponse response,
            @CookieValue(name = "rt_session_id", required = true) Cookie refreshTokenCookieRequest
    ){
        String refreshToken = jwtService.findRefreshById(refreshTokenCookieRequest.getValue()).orElseThrow(() -> new TokenNotFoundException("Token not found."));
        Claims claims = jwtService.verifyToken(refreshToken);

        Long userId = ((Integer) claims.get("id")).longValue();

        UserDto userDto = userService.findUserById(userId).orElseThrow(() -> new TokenException("User not found with given refresh token.", HttpStatus.UNAUTHORIZED));

        AccessTokenResponse accessToken = jwtService.createAccessToken(userDto.getId(), userDto.getEmail());

        return ResponseEntity.ok().body(accessToken);

    }


    @GetMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            @CookieValue(name = "rt_session_id", required = false) Cookie refreshCookie
    ){
        if (refreshCookie == null){
            return ResponseEntity.ok().body("User is already logged out.");
        }

        Cookie emptyCookie = authService.logoutUser(refreshCookie);
        response.addCookie(emptyCookie);

        return ResponseEntity.ok().body("User logged out successfully.");
    }
}
