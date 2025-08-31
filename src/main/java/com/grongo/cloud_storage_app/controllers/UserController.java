package com.grongo.cloud_storage_app.controllers;


import com.grongo.cloud_storage_app.models.user.dto.UserDto;
import com.grongo.cloud_storage_app.services.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/{userId}/updatePicture")
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateProfilePicture(
            @RequestParam MultipartFile file,
            @PathVariable Long userId
    ){
        return userService.changeUserPicture(userId, file);
    }

    @PatchMapping("/{userId}/rename")
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateProfilePicture(
            @RequestParam String username,
            @PathVariable Long userId
    ){
        return userService.updateUsername(userId, username);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable Long userId
    ){
        userService.deleteUser(userId);
    }
}
