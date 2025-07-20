package com.grongo.cloud_storage_app.models.sharedItems.dto;


import com.grongo.cloud_storage_app.services.sharedItems.FileRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SharedItemRequest {


    @Email(message = "Enter a valid email format.")
    @NotBlank(message = "Email can't be blank.")
    @NotNull(message = "Email can't be null.")
    private String email;

    @NotNull(message = "Item id can't be null.")
    private Long itemId;

    @NotNull(message = "File role can't be null.")
    private FileRole fileRole;

}
