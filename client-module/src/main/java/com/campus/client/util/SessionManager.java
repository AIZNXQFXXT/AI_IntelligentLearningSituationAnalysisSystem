package com.campus.client.util;

import javafx.application.Platform;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();
    private String token;
    private Long userId;
    private String username;
    private String role;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isTeacher() {
        return "TEACHER".equals(role);
    }

    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    public void clear() {
        this.token = null;
        this.userId = null;
        this.username = null;
        this.role = null;
    }
}
