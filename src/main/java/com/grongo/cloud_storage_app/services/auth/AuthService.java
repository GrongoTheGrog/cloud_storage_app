package com.grongo.cloud_storage_app.services.auth;

import com.grongo.cloud_storage_app.models.user.dto.RequestUser;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import jakarta.servlet.http.Cookie;

public interface AuthService {
    public UserDto createUserCredentials(RequestUser requestUser);
    public UserDto authenticateUserCredentials(RequestUser requestUser);
    public Cookie logoutUser(Cookie refreshCookie);
}
