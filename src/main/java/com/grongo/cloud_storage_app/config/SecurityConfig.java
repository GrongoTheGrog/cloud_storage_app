package com.grongo.cloud_storage_app.config;


import com.grongo.cloud_storage_app.security.JwtFilterCheck;
import com.grongo.cloud_storage_app.security.CustomAuthenticationEntrypoint;
import com.grongo.cloud_storage_app.security.CustomOauth2SuccessHandler;
import com.grongo.cloud_storage_app.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity security,
            CustomAuthenticationEntrypoint authenticationEntrypoint,
            JwtFilterCheck jwtFilterCheck,
            CustomOauth2SuccessHandler successHandler,
            CustomUserDetailsService userDetailsService
    ) throws Exception {
        security
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(customizer -> {
            customizer
                    .requestMatchers(
                            "/",
                            "/api/auth/**",
                            "/oauth2/**",
                            "/login/oauth2/code/**",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/swagger-resources/**",
                            "/webjars/**" )
                    .permitAll()
                    .anyRequest()
                    .authenticated();
                })
                .oauth2Login(config -> config
                        .successHandler(successHandler)
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/api/auth/login")
                        )
                )
                .addFilterBefore(jwtFilterCheck, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e ->
                        e.authenticationEntryPoint(authenticationEntrypoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return security.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
