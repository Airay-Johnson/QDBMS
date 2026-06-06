package com.qdbms.controller;

import com.qdbms.common.Result;
import com.qdbms.dto.LoginRequest;
import com.qdbms.dto.LoginResponse;
import com.qdbms.dto.RegisterRequest;
import com.qdbms.entity.User;
import com.qdbms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/register")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        return Result.ok(authService.register(request));
    }

    @GetMapping("/me")
    public Result<User> currentUser(Authentication auth) {
        return Result.ok(authService.getCurrentUser(auth.getName()));
    }
}
