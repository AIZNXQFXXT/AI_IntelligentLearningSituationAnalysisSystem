package com.campus.backend.service;

import com.campus.backend.entity.User;
import com.campus.common.dto.LoginDTO;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(LoginDTO dto);
    User getCurrentUser(Long userId);
    void logout(Long userId, String token);
    Map<String, Object> refresh(String refreshToken);
    void updateProfile(Long userId, Map<String, Object> updates);
    void changePassword(Long userId, String oldPassword, String newPassword);
}