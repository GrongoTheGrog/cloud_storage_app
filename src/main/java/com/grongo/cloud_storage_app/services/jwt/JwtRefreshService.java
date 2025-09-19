package com.grongo.cloud_storage_app.services.jwt;

import com.grongo.cloud_storage_app.exceptions.auth.AccessDeniedException;
import com.grongo.cloud_storage_app.models.token.JwtRefresh;
import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import com.grongo.cloud_storage_app.repositories.RefreshRepository;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import com.grongo.cloud_storage_app.services.cache.CacheKeys;
import com.grongo.cloud_storage_app.services.cache.impl.RefreshTokenCodeCache;
import jakarta.servlet.http.Cookie;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtRefreshService extends Jwt{

    @Autowired
    private RefreshRepository refreshRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RefreshTokenCodeCache refreshTokenCodeCache;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenCodeCache cache;

    public final String TOKEN_NAME = "rt_session_id";

    JwtRefreshService() {
        super(Duration.ofDays(30));
    }

    @Override
    public String create(Long userId, String email) {
        return createJwt(userId, email);
    }

    public String persistToDbAndReturnId(UserDto userDto){
        User user = modelMapper.map(userDto, User.class);
        return persistToDbAndReturnId(user);
    }

    public String persistToDbAndReturnId(User user){
        String token = createJwt(user.getId(), user.getEmail());
        String id = UUID.randomUUID().toString();

        JwtRefresh jwtRefresh = JwtRefresh.builder()
                .user(user)
                .token(token)
                .id(id)
                .expirationDate(new Date(System.currentTimeMillis() + duration.getSeconds() * 1000))
                .build();
        refreshRepository.save(jwtRefresh);

        return id;
    }

    public String getIdFromCode(String code){
        String tokenId = refreshTokenCodeCache.getKey(CacheKeys.refreshTokenKey(code));
        if (tokenId == null) throw new AccessDeniedException("Invalid code.");

        return tokenId;
    }

    public Optional<JwtRefresh> findById(String id){
        return refreshRepository.findById(id);
    }

    public void deleteRefreshToken(String id){
        refreshRepository.deleteById(id);
    }

    public void deleteRefreshTokensByUserId(Long userId){
        refreshRepository.deleteByUserId(userId);
    }

    public Cookie cookie(String value) {
        return createJwtCookie(TOKEN_NAME, value);
    }

    public Cookie emptyCookie() {
        return createJwtCookie(TOKEN_NAME, "");
    }
}

