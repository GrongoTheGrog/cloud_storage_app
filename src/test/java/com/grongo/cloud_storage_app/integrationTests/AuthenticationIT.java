package com.grongo.cloud_storage_app.integrationTests;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.models.token.dto.AccessTokenResponse;
import com.grongo.cloud_storage_app.models.user.dto.RegisterUser;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import com.grongo.cloud_storage_app.repositories.RefreshRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.auth.AuthService;
import com.grongo.cloud_storage_app.services.auth.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.*;
import java.util.Optional;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static com.grongo.cloud_storage_app.TestUtils.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest
public class AuthenticationIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshRepository refreshRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;


    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testIfUserIsCreatedOnSignup() throws Exception {
        String requestUserJson = getRequestUserJson();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestUserJson)
        ).andExpect(status().isCreated());

        Optional<User> user = userRepository.findById(1L);
        assertThat(user).isPresent();
    }

    @Test
    public void testIfUserCanBeAuthenticated() throws Exception {
        RegisterUser registerUser = getRequestUser();
        String authenticateUserJson = getAuthenticateUserJson();

        authService.createUserCredentials(registerUser);

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(authenticateUserJson)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(cookie().exists("rt_session_id"));
    }

    @Test
    public void testIfRefreshEndpointReturnsAnAccessToken() throws Exception {

        RegisterUser registerUser = getRequestUser();

        UserDto userDto = authService.createUserCredentials(registerUser);
        Cookie refreshTokenCookie = jwtService.getRefreshTokenCookie(userDto.getId(), userDto.getEmail(), userDto);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/auth/refresh")
                .cookie(refreshTokenCookie)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    public void testIfUserCanLogOut() throws Exception {
        RegisterUser registerUser = getRequestUser();

        UserDto userDto = authService.createUserCredentials(registerUser);
        Cookie refreshTokenCookie = jwtService.getRefreshTokenCookie(userDto.getId(), userDto.getEmail(), userDto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/logout")
                .cookie(refreshTokenCookie)
        ).andExpect(cookie().value("rt_session_id", ""));

        String tokenId = refreshTokenCookie.getValue();
        Optional<String> jwtRefresh = jwtService.findRefreshById(tokenId);
        assertThat(jwtRefresh).isEmpty();
    }

    @Test
    public void testIfJwtBlocksAccessWithoutJwt() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/ping")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void testIfJwtAllowAccessWithJwt() throws Exception {
        RegisterUser registerUser = getRequestUser();
        UserDto userDto = authService.createUserCredentials(registerUser);

        AccessTokenResponse accessTokenResponse = jwtService.createAccessToken(userDto.getId(), userDto.getEmail());

        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/ping")
                .header("Authorization", "Bearer " + accessTokenResponse.getAccessToken())
        ).andExpect(status().isOk());
    }
}
