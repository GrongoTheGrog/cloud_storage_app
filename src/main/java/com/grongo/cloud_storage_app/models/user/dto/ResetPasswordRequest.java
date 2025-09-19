package com.grongo.cloud_storage_app.models.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordRequest {

    @Email
    @NotBlank
    @NotNull
    String email;

    @Valid
    @NotBlank
    @Size(max = 40, min = 10, message = "The password must contain at least {min} and at most {max} characters.")
    String password;
}
