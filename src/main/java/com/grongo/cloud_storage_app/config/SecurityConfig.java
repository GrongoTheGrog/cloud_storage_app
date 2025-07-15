package com.grongo.cloud_storage_app.config;


import com.grongo.cloud_storage_app.security.JwtFilterCheck;
import com.grongo.cloud_storage_app.security.CustomAuthenticationEntrypoint;
import com.grongo.cloud_storage_app.security.CustomOauth2SuccessHandler;
import com.grongo.cloud_storage_app.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService
    ) throws Exception {
        security
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(customizer -> {
            customizer
                    .requestMatchers("/", "/api/auth/**")
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
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return security.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            PasswordEncoder passwordEncoder,
            CustomUserDetailsService userDetailsService
    ) throws Exception {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }
}
