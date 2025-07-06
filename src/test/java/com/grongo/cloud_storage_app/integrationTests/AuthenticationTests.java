package com.grongo.cloud_storage_app.integrationTests;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.TestUtils;
import com.grongo.cloud_storage_app.models.User;
import com.grongo.cloud_storage_app.models.dto.AccessTokenResponse;
import com.grongo.cloud_storage_app.repositories.RefreshRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.assertj.core.api.Assertions.*;
import java.util.Optional;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
public class AuthenticationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshRepository refreshRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestUtils testUtils;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testIfUserIsCreatedOnSignup() throws Exception {
        String requestUserJson = testUtils.getRequestUserJson();

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
        String requestUserJson = testUtils.getRequestUserJson();

        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestUserJson)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(cookie().exists("rt_session_id"));
    }

    @Test
    public void testIfRefreshEndpointReturnsAnAccessToken() throws Exception {

        String requestUserJson = testUtils.getRequestUserJson();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestUserJson)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(cookie().exists("rt_session_id"))
                .andReturn();

        Cookie refreshCookie = result.getResponse().getCookie("rt_session_id");

        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/auth/refresh")
                .cookie(refreshCookie)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    public void testIfUserCanLogOut() throws Exception {
        String requestUserJson = testUtils.getRequestUserJson();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestUserJson)
                )
                .andReturn();

        Cookie refreshCookie = result.getResponse().getCookie("rt_session_id");
    }

    @Test
    public void testIfJwtBlocksAccessWithoutJwt() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/ping")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    public void testIfJwtAllowAccessWithJwt() throws Exception {
        String requestUserJson = testUtils.getRequestUserJson();

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestUserJson)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(cookie().exists("rt_session_id"))
                .andReturn();

        AccessTokenResponse accessTokenResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AccessTokenResponse.class);
        String accessToken = accessTokenResponse.getAccessToken();

        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/ping")
                .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());
    }
}
