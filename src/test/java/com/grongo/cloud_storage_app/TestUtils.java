package com.grongo.cloud_storage_app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.user.dto.RequestUser;
import org.springframework.stereotype.Component;

@Component
public class TestUtils {

    ObjectMapper objectMapper = new ObjectMapper();

    public RequestUser getRequestUser(){
        return RequestUser.builder()
                .username("test")
                .email("test")
                .password("test")
                .build();
    }

    public String getRequestUserJson() throws JsonProcessingException {
        RequestUser requestUser = getRequestUser();
        return objectMapper.writeValueAsString(requestUser);
    }

}
