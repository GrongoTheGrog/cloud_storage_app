package com.grongo.cloud_storage_app.models.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    @Email(message = "Invalid email format.")
    private String email;

    @NotBlank
    private String password;
}
