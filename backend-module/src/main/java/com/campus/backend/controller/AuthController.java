package com.campus.backend.controller;

import com.campus.backend.service.AuthService;
import com.campus.common.dto.LoginDTO;
import com.campus.common.dto.PasswordChangeDTO;
import com.campus.common.dto.RefreshTokenDTO;
import com.campus.common.vo.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String authHeader = request.getHeader("Authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7) : null;
        authService.logout(userId, token);
        return ApiResponse.success();
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh(@Valid @RequestBody RefreshTokenDTO dto) {
        Map<String, Object> result = authService.refresh(dto.getRefreshToken());
        return ApiResponse.success(result);
    }

    @PatchMapping("/profile")
    public ApiResponse<Void> updateProfile(@RequestBody Map<String, Object> updates,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        authService.updateProfile(userId, updates);
        return ApiResponse.success();
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeDTO dto,
                                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        authService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return ApiResponse.success();
    }
}