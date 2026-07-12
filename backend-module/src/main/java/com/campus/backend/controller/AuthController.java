package com.campus.backend.controller;

import com.campus.backend.security.JwtUtil;
import com.campus.backend.service.AuthService;
import com.campus.common.dto.LoginDTO;
import com.campus.common.vo.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        Map<String, Object> result = authService.login(dto);
        return ApiResponse.success(result);
    }
}