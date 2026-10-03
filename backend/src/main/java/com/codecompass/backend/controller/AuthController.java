package com.codecompass.backend.controller;

import com.codecompass.backend.dto.RegisterRequest;
import com.codecompass.backend.entity.User;
import com.codecompass.backend.service.UserService;
import com.codecompass.backend.dto.LoginRequest;
import com.codecompass.backend.dto.LoginResponse;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/auth/register")
    public String register(@Valid @RequestBody RegisterRequest request) {

        User user = userService.register(request);

        return "Registration successful";
    }

       
    @PostMapping("/api/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        String token = userService.login(request);

        return new LoginResponse(token);
    }
}