package com.example.campusactivity.controller;

import com.example.campusactivity.common.ApiResponse;
import com.example.campusactivity.dto.LoginRequest;
import com.example.campusactivity.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("login success", authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<?> me() {
        return ApiResponse.ok(authService.currentUser());
    }
}
