package com.qdbms.service.impl;

import com.qdbms.dto.LoginRequest;
import com.qdbms.dto.LoginResponse;
import com.qdbms.dto.RegisterRequest;
import com.qdbms.entity.User;
import com.qdbms.entity.UserRole;
import com.qdbms.mapper.UserMapper;
import com.qdbms.mapper.UserRoleMapper;
import com.qdbms.security.JwtTokenProvider;
import com.qdbms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userMapper.selectByUsername(request.getUsername());
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .toList();

        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getId(), roles);
        return new LoginResponse(token, user.getUsername(), user.getId());
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setIsActive(true);
        userMapper.insert(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(2L); // USER 角色
        userRoleMapper.insert(userRole);

        return user;
    }

    @Override
    public User getCurrentUser(String username) {
        User user = userMapper.selectByUsername(username);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }
}
