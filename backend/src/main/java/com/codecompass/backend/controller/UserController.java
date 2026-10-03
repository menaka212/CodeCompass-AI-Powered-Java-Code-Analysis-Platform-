package com.codecompass.backend.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.codecompass.backend.dto.UpdateUserRequest;
import com.codecompass.backend.dto.UserResponse;
import com.codecompass.backend.entity.User;
import com.codecompass.backend.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.validation.Valid;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


@GetMapping("/api/users/me")
public UserResponse getCurrentUser(Authentication authentication) {

    Long userId = (Long) authentication.getPrincipal();

    User user = userService.getUserById(userId);

    return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail()
    );
}
@PutMapping("/api/users/me")
public UserResponse updateCurrentUser(
        Authentication authentication,
        @Valid @RequestBody UpdateUserRequest request) {

    Long userId = (Long) authentication.getPrincipal();

    User user = userService.updateUser(userId, request);

    return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail()
    );
}

@DeleteMapping("/api/users/me")
public Map<String, String> deleteCurrentUser(
        Authentication authentication) {

    Long userId = (Long) authentication.getPrincipal();

    userService.deleteUser(userId);

    return Map.of(
            "message", "User account deleted successfully"
    );
}
}