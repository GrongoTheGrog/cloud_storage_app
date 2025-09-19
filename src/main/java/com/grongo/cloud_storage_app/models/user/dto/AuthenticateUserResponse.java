package com.grongo.cloud_storage_app.models.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticateUserResponse {

    Long id;
    String username;
    String email;
    String picture;
    String accessToken;

}
