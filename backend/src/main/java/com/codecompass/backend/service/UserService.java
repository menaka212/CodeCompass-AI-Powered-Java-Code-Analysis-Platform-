package com.codecompass.backend.service;

import com.codecompass.backend.dto.RegisterRequest;
import com.codecompass.backend.dto.UpdateUserRequest;
import com.codecompass.backend.entity.User;
import com.codecompass.backend.repository.UserRepository;
import com.codecompass.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.codecompass.backend.dto.LoginRequest;
import com.codecompass.backend.exception.UserAlreadyExistsException;
import com.codecompass.backend.exception.UserNotFoundException;
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    public User getUserById(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
    }
    public User register(RegisterRequest request) {

        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
public String login(LoginRequest request) {

    System.out.println("LOGIN EMAIL RECEIVED: [" + request.getEmail() + "]");

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() ->
                    new RuntimeException("Invalid email or password"));

    System.out.println("USER FOUND: " + user.getEmail());

    if (!passwordEncoder.matches(
            request.getPassword(),
            user.getPassword())) {

        throw new RuntimeException("Invalid email or password");
    }

    return jwtService.generateToken(user.getId(), user.getEmail());
}
public void deleteUser(Long userId) {

    User user = getUserById(userId);

    userRepository.delete(user);
}
public User updateUser(Long userId, UpdateUserRequest request) {

    User user = getUserById(userId);

    // Check whether another user already uses this username
    if (!user.getUsername().equals(request.getUsername())
            && userRepository.existsByUsername(request.getUsername())) {

        throw new UserAlreadyExistsException(
                "Username already exists"
        );
    }

    // Check whether another user already uses this email
    if (!user.getEmail().equals(request.getEmail())
            && userRepository.existsByEmail(request.getEmail())) {

        throw new UserAlreadyExistsException(
                "Email already exists"
        );
    }

    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());

    return userRepository.save(user);
}
}