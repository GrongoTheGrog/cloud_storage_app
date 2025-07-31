package com.grongo.cloud_storage_app.services.auth;

import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.models.user.dto.AuthenticateUser;
import com.grongo.cloud_storage_app.models.user.dto.RegisterUser;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import jakarta.servlet.http.Cookie;

public interface AuthService {

    public UserDto createUserCredentials(RegisterUser registerUser);
    public UserDto authenticateUserCredentials(AuthenticateUser authenticateUser);
    public Cookie logoutUser(Cookie refreshCookie);
    public User getCurrentAuthenticatedUser();
}
