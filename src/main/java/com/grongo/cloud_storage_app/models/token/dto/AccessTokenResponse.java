package com.grongo.cloud_storage_app.models.token.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenResponse {
    private String accessToken;
    private int expiresAt;
}
