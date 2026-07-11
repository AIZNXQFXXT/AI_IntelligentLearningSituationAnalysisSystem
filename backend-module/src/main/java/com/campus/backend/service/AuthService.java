package com.campus.backend.service;

import com.campus.backend.entity.User;

public interface AuthService {
    String login(String username, String password);
    User getCurrentUser(Long userId);
}