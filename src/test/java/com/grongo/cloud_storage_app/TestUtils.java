package com.grongo.cloud_storage_app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.user.dto.RegisterUser;
import org.springframework.stereotype.Component;

@Component
public class TestUtils {

    ObjectMapper objectMapper = new ObjectMapper();

    public RegisterUser getRequestUser(){
        return RegisterUser.builder()
                .username("test")
                .email("test")
                .password("test")
                .build();
    }

    public String getRequestUserJson() throws JsonProcessingException {
        RegisterUser registerUser = getRequestUser();
        return objectMapper.writeValueAsString(registerUser);
    }

}
