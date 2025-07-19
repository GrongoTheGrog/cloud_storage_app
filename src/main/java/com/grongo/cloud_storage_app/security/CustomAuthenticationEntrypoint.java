package com.grongo.cloud_storage_app.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.models.exceptions.ExceptionResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntrypoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException
    ) throws IOException, ServletException {
        String exceptionResponse = new ObjectMapper().writeValueAsString(new ExceptionResponse(
                401,
                "Bad credentials",
                authException.toString()
        ));

        response.setStatus(401);
        response.setContentType("application/json");
        response.getWriter().write(exceptionResponse);
    }
}
