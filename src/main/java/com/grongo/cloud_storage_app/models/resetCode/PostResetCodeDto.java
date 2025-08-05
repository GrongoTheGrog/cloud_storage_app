package com.grongo.cloud_storage_app.models.resetCode;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.scheduling.annotation.Async;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostResetCodeDto {

    @Email(message = "Wrong email format.")
    @NotNull
    @NotBlank
    String email;
}
