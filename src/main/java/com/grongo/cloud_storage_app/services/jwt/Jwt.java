package com.grongo.cloud_storage_app.services.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class Jwt {

    @Value("${JWT_SECRET}")
    String key;

    protected Duration duration;

    public Jwt(Duration duration){
        this.duration = duration;
    }

    abstract public String create(Long userId, String email);

    public SecretKey getKey(){
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    protected String createJwt(Long userId, String userEmail){
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("email", userEmail);

        return Jwts.builder()
                .signWith(getKey())
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis() + duration.getSeconds() * 1000))
                .subject(userId.toString())
                .issuer("http:/localhost/8080")
                .compact();
    }

    protected Cookie createJwtCookie(String name, String value){
        Cookie tokenCookie = new Cookie(name, value);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setMaxAge((int) duration.getSeconds());
        tokenCookie.setAttribute("SameSite", "None");
        tokenCookie.setSecure(true);
        tokenCookie.setPath("/");
        return tokenCookie;
    }

    public Claims verify(String token){
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }
}
