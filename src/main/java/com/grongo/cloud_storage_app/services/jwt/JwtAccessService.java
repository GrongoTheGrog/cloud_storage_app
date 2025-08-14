package com.grongo.cloud_storage_app.services.jwt;

import com.grongo.cloud_storage_app.models.token.dto.AccessTokenResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
public class JwtAccessService extends Jwt{

    public final String TOKEN_NAME = "at_value";

    public JwtAccessService(){
        super(Duration.ofMinutes(5));
    }

    @Override
    public String create(Long userId, String email) {
        return createJwt(userId, email);
    }

    public AccessTokenResponse formatResponse(String accessToken){
        return AccessTokenResponse.builder()
                .accessToken(accessToken)
                .expiresInSeconds(duration.getSeconds())
                .build();
    }
}
