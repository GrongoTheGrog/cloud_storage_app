package com.grongo.cloud_storage_app.integrationTests;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.items.Folder;
import com.grongo.cloud_storage_app.models.items.dto.*;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.repositories.FolderRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.auth.JwtService;
import com.grongo.cloud_storage_app.services.items.FolderService;
import com.grongo.cloud_storage_app.services.items.StorageService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.amazon.awssdk.services.s3.S3Client;

import static org.assertj.core.api.Assertions.*;
import static com.grongo.cloud_storage_app.TestUtils.*;

import java.util.List;
import java.util.Optional;

@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class StorageIT {

    @MockitoSpyBean
    S3Client s3Client;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StorageService storageService;

    @Autowired
    JwtService jwtService;

    @Autowired
    FolderRepository folderRepository;

    @Autowired
    FolderService folderService;

    private String accessToken;
    private User currentAuthenticatedUser;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    //GET AN ACCESS TOKEN TO BE ABLE TO PERFORM REQUESTS
    public void authenticate(){
        User user = User.builder()
                .username("test")
                .email("test")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        userRepository.save(user);
        currentAuthenticatedUser = user;

        accessToken = jwtService.createAccessToken(user.getId(), "test").getAccessToken();
    }


    @Test
    public void shouldItemPathBeUpdatedIfMoved() throws Exception {
        Folder folder = getFolder("folder", null, currentAuthenticatedUser);
        folder.setPath("/folder");
        folderRepository.save(folder);

        Folder folder1 = getFolder("folder1", folder, currentAuthenticatedUser);
        folder1.setPath("/folder1");
        folderRepository.save(folder1);

        MoveItemRequest moveItemRequest = new MoveItemRequest(null);
        String moveItemRequestJson = objectMapper.writeValueAsString(moveItemRequest);

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/items/move/" + folder1.getId())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(moveItemRequestJson)
        ).andExpect(status().isNoContent());

        Optional<Folder> foundFolder = folderRepository.findById(folder1.getId());
        assertThat(foundFolder).isPresent();
        assertThat(foundFolder.get().getPath()).isEqualTo("/folder1");
    }

}
