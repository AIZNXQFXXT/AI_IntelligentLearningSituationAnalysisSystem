package com.campus.backend.controller;

import com.campus.backend.entity.User;
import com.campus.backend.security.JwtUtil;
import com.campus.backend.service.AuthService;
import com.campus.common.vo.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String token = authService.login(body.get("username"), body.get("password"));
        Long userId = jwtUtil.getUserId(token);
        User user = authService.getCurrentUser(userId);
        return ApiResponse.success(Map.of(
                "token", token,
                "role", user.getRole(),
                "username", user.getUsername(),
                "userId", user.getId()
        ));
    }
}