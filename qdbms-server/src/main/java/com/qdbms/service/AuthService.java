package com.qdbms.service;

import com.qdbms.dto.LoginRequest;
import com.qdbms.dto.LoginResponse;
import com.qdbms.dto.RegisterRequest;
import com.qdbms.entity.User;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    User register(RegisterRequest request);
    User getCurrentUser(String username);
}
