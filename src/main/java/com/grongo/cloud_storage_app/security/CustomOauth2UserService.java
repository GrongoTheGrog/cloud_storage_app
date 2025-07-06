package com.grongo.cloud_storage_app.security;

import com.grongo.cloud_storage_app.models.User;
import com.grongo.cloud_storage_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        OAuth2User auth2User = delegate.loadUser(userRequest);

        String username = auth2User.getName();
        String email = auth2User.getAttribute("email");

        User user = User.builder()
                .username(username)
                .email(email)
                .build();

        userRepository.save(user);

        return new DefaultOAuth2User(
                auth2User.getAuthorities(),
                auth2User.getAttributes(),
                auth2User.getName()
        );
    }
}
