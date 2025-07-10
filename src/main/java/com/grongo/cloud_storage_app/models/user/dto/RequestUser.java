package com.grongo.cloud_storage_app.models.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestUser {

    private String username;
    private String password;
    private String email;
    private String picture;

}
