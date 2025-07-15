package com.grongo.cloud_storage_app.services.auth;

import com.grongo.cloud_storage_app.models.user.User;
import com.grongo.cloud_storage_app.models.user.dto.AuthenticateUser;
import com.grongo.cloud_storage_app.models.user.dto.RegisterUser;
import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import jakarta.servlet.http.Cookie;

public interface AuthService {

    /**
     * Creates a user and assign to database
     * @param registerUser the request body format to create a user
     * @return user dto
     */
    public UserDto createUserCredentials(RegisterUser registerUser);

    /**
     * Authenticates user with the given password and email
     * @param authenticateUser the request body format to log in a user
     * @return user dto
     */
    public UserDto authenticateUserCredentials(AuthenticateUser authenticateUser);

    /**
     * Deletes the refresh token in the database
     * @param refreshCookie the request body format to create a user
     * @return return an empty cookie to remove rt_session_id
    */
    public Cookie logoutUser(Cookie refreshCookie);

    /**
     * Gets the current authenticated user
     * @return user
     */
    public User getCurrentAuthenticatedUser();
}
