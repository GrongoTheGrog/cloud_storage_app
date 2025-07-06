package com.grongo.cloud_storage_app.services.auth;


import com.grongo.cloud_storage_app.exceptions.InvalidTokenException;
import com.grongo.cloud_storage_app.models.JwtRefresh;
import com.grongo.cloud_storage_app.models.User;
import com.grongo.cloud_storage_app.models.dto.AccessTokenResponse;
import com.grongo.cloud_storage_app.models.dto.UserDto;
import com.grongo.cloud_storage_app.repositories.RefreshRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.*;

@Service
public class JwtService {

    @Value("${JWT_SECRET}")
    String key;

    //15 MINUTES
    int accessTokenExpiration = 1000 * 60 * 15;

    //10 DAYS
    int refreshTokenExpiration = 1000 * 60 * 60 * 24 * 10;

    @Autowired
    private RefreshRepository refreshRepository;

    @Autowired
    private ModelMapper modelMapper;

    private String createJwt(Long userId, String userEmail, int expiration){
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("email", userEmail);

        return Jwts.builder()
                .signWith(getKey())
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis() + expiration))
                .subject(userId.toString())
                .issuer("http:/localhost/8080")
                .compact();
    }


    public AccessTokenResponse createAccessToken(Long userId, String userEmail){
        return AccessTokenResponse.builder()
                .accessToken(createJwt(userId, userEmail, accessTokenExpiration))
                .expiresAt(accessTokenExpiration)
                .build();
    }

    //CREATES THE TOKEN AND PERSISTS TO THE DATABASE
    public String createRefreshToken(Long userId, String userEmail, UserDto userDto){
        User user = modelMapper.map(userDto, User.class);
        String token = createJwt(userId, userEmail, refreshTokenExpiration);
        String id = UUID.randomUUID().toString();

        JwtRefresh jwtRefresh = JwtRefresh.builder().user(user).token(token).id(id).expirationDate(new Date(System.currentTimeMillis() + refreshTokenExpiration)).build();
        refreshRepository.save(jwtRefresh);

        return id;
    }

    //VERIFY AND GETS THE CLAIMS
    public Claims verifyToken(String token){
        try{
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }catch (Exception e){
            throw new InvalidTokenException(e.getMessage());
        }

    }

    //CREATE REFRESH TOKEN AND COOKIE
    public Cookie getRefreshTokenCookie(Long userId, String userEmail, UserDto userDto){
        String id = createRefreshToken(userId, userEmail, userDto);
        Cookie tokenCookie = new Cookie("rt_session_id", id);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setMaxAge(refreshTokenExpiration);
        tokenCookie.setSecure(false);

        return tokenCookie;
    }


    //DELETE REFRESH TOKEN IN THE DATABASE
    public void deleteRefreshToken(Cookie refreshCookie){
       refreshRepository.deleteById(refreshCookie.getValue());
    }

    //GET REFRESH TOKEN FROM DATABASE
    public Optional<String> findRefreshById(String id){
        return refreshRepository.findById(id).map(JwtRefresh::getToken);
    }

    //CREATE EMPTY COOKIE FOR REFRESH TOKEN ID
    public Cookie createEmptyRefreshIdCookie(){
        Cookie emptyCookie = new Cookie("rt_session_id", "");
        emptyCookie.setHttpOnly(true);
        emptyCookie.setMaxAge(refreshTokenExpiration);
        emptyCookie.setSecure(false);

        return emptyCookie;
    }

    //TRANSFORM STRING TO KEY
    public SecretKey getKey(){
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }
}
