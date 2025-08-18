package com.grongo.cloud_storage_app.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.InvalidTokenException;
import com.grongo.cloud_storage_app.exceptions.tokenExceptions.MissingTokenException;
import com.grongo.cloud_storage_app.models.exceptions.ExceptionResponse;
import com.grongo.cloud_storage_app.services.jwt.JwtAccessService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtFilterCheck extends OncePerRequestFilter {

    private final JwtAccessService jwtAccessService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
            ) throws ServletException, IOException {

        try{

            String bearerHeader = request.getHeader("Authorization");

            if (bearerHeader == null){
                throw new MissingTokenException();
            }

            String accessToken = bearerHeader.substring(7);

            if (accessToken.equals("null")) throw new MissingTokenException();

            //VERIFIES THE TOKEN AND THROW IF INVALID
            Claims claims = jwtAccessService.verify(accessToken);

            Authentication auth = new UsernamePasswordAuthenticationToken(claims.get("email"), null, List.of());
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        }catch (MissingTokenException e){
            writeErrorResponse(401, "Missing token.", e.getMessage(), response);
        }catch (InvalidTokenException e){
            writeErrorResponse(401, "Invalid token.", e.getMessage(), response);
        }catch (Exception e){
            writeErrorResponse(401, "Error parsing token.", e.getMessage(), response);
        }

    }

    private void writeErrorResponse(int status, String message, String details, HttpServletResponse response) throws IOException {
        ExceptionResponse exceptionResponse = new ExceptionResponse(status, message, details, true);
        ObjectMapper objectMapper = new ObjectMapper();

        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));

    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        List<String> cleanUrls = List.of(
                "/api/auth",
                "/login",
                "oauth2",
                "/swagger",
                "/v3/api-docs",
                "/webjars"
        );

        for (String url : cleanUrls){

            if (request.getRequestURI().contains(url)) return true;
        }

        return false;
    }
}
