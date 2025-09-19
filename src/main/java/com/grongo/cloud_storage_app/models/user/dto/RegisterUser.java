package com.grongo.cloud_storage_app.models.user.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUser {
    @Valid
    @NotBlank
    @Size(max = 40, min = 4, message = "The username must contain at least {min} and at most {max} characters.")
    private String username;

    @Valid
    @NotBlank
    @Size(max = 40, min = 10, message = "The password must contain at least {min} and at most {max} characters.")
    private String password;

    @Valid
    @Email(message = "Invalid email format")
    private String email;

    private String picture;
}
