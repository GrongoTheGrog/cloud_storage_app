package com.grongo.cloud_storage_app.models.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * dto used for login body requests
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticateUser {
    private String email;
    private String password;
}
