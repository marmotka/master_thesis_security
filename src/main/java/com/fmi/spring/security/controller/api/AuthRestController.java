package com.fmi.spring.security.controller.api;

import com.fmi.spring.security.dto.AuthResponse;
import com.fmi.spring.security.dto.LoginRequest;
import com.fmi.spring.security.dto.RegisterRequest;
import com.fmi.spring.security.exception.DuplicateUserException;
import com.fmi.spring.security.model.User;
import com.fmi.spring.security.security.JwtService;
import com.fmi.spring.security.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private final JwtService jwtService;



    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        User user;
        try {
            user = userService.register(request);
        } catch (DuplicateUserException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }

        // Create UserDetails object for JWT generation
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword()) // not used in JWT
                .authorities("ROLE_" + user.getRole())
                .build();

        String token = jwtService.generateToken(userDetails);

        AuthResponse response = new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().toString(),
                token
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);


    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        Optional<User> userOpt = userService.authenticate(request);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }

        User user = userOpt.get();

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole())
                .build();

        String token = jwtService.generateToken(userDetails);

        AuthResponse response = new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().toString(),
                token
        );

        return ResponseEntity.ok(response);
    }
}
