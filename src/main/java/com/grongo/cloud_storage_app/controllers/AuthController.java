package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.exceptions.tokenExceptions.TokenNotFoundException;
import com.grongo.cloud_storage_app.models.resetCode.CheckResetCodeRequest;
import com.grongo.cloud_storage_app.models.resetCode.PostResetCodeDto;
import com.grongo.cloud_storage_app.models.token.JwtRefresh;
import com.grongo.cloud_storage_app.models.token.dto.AccessTokenResponse;
import com.grongo.cloud_storage_app.models.user.dto.*;
import com.grongo.cloud_storage_app.services.auth.impl.AuthServiceImpl;
import com.grongo.cloud_storage_app.services.jwt.JwtAccessService;
import com.grongo.cloud_storage_app.services.jwt.JwtRefreshService;
import com.grongo.cloud_storage_app.services.resetCode.ResetCodeService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;
    private final ResetCodeService resetCodeService;
    private final JwtAccessService jwtAccessService;
    private final JwtRefreshService jwtRefreshService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AuthenticateUserResponse> signup(
            @Validated @RequestBody RegisterUser registerUser,
            HttpServletResponse response
    ){
        UserDto userDto = authService.createUserCredentials(registerUser);

        String accessToken = jwtAccessService.create(userDto.getId(), userDto.getEmail());

        String tokenId = jwtRefreshService.persistToDbAndReturnId(userDto);
        Cookie refreshTokenCookie = jwtRefreshService.cookie(tokenId);
        response.addCookie(refreshTokenCookie);

        AuthenticateUserResponse userResponse = AuthenticateUserResponse.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .id(userDto.getId())
                .accessToken(accessToken)
                .picture(userDto.getPicture())
                .build();


        return ResponseEntity.ok().body(userResponse);
    }


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AuthenticateUserResponse> login(
            @RequestBody AuthenticateUser authenticateUser,
            HttpServletRequest request,
            HttpServletResponse response
            ){
        UserDto userDto = authService.authenticateUserCredentials(authenticateUser);

        String accessToken = jwtAccessService.create(userDto.getId(), userDto.getEmail());

        String tokenId = jwtRefreshService.persistToDbAndReturnId(userDto);
        Cookie refreshTokenCookie = jwtRefreshService.cookie(tokenId);
        response.addCookie(refreshTokenCookie);

        AuthenticateUserResponse authenticateUserResponse = AuthenticateUserResponse.builder()
                .username(userDto.getUsername())
                .id(userDto.getId())
                .email(userDto.getEmail())
                .picture(userDto.getPicture())
                .accessToken(accessToken)
                .build();

        return ResponseEntity.ok(authenticateUserResponse);
    }


    @GetMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public AccessTokenResponse refresh(
            @CookieValue(name = "rt_session_id", required = true) Cookie refreshTokenCookieRequest
    ){
        JwtRefresh refreshToken = jwtRefreshService
                .findById(refreshTokenCookieRequest.getValue())
                .orElseThrow(() -> new TokenNotFoundException("Token not found."));

        Claims claims = jwtRefreshService.verify(refreshToken.getToken());

        Number id = (Number) claims.get("id");
        String accessToken = jwtAccessService.create(id.longValue(), (String) claims.get("email"));
        return jwtAccessService.formatResponse(accessToken);
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

        Cookie emptyCookie = jwtRefreshService.emptyCookie();
        response.addCookie(emptyCookie);

        return ResponseEntity.ok().body("User logged out successfully.");
    }

    @PostMapping("/resetCode")
    @ResponseStatus(HttpStatus.CREATED)
    public void createResetCode(
            @Validated @RequestBody PostResetCodeDto codeDto
            ){
        resetCodeService.createCode(codeDto.getEmail());
    }

    @PostMapping("/resetCode/check")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void checkResetCode(
            @Validated @RequestBody CheckResetCodeRequest resetCodeRequest
            ){
        resetCodeService.checkCode(resetCodeRequest);
    }

    @PatchMapping("/resetPassword")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(
            @Validated @RequestBody ResetPasswordRequest resetPasswordRequest
            ){
        authService.resetPassword(resetPasswordRequest);
    }

    @GetMapping("/refreshToken")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestRefreshToken(
            @RequestParam String code,
            HttpServletResponse response
    ){
        String refreshTokenId = jwtRefreshService.getIdFromCode(code);
        Cookie refreshTokenCookie = jwtRefreshService.cookie(refreshTokenId);
        response.addCookie(refreshTokenCookie);
    }

    @GetMapping("/getCsrfToken")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CsrfToken> getCsrfToken(CsrfToken csrfToken){
        return ResponseEntity.ok(csrfToken);
    }
}
