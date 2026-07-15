package com.aicampus.session;

import com.aicampus.model.LoginResult;
import com.aicampus.service.ApiClient;
import com.aicampus.util.AuthStorage;

import java.util.Map;

public class UserSession {
    private static UserSession instance;
    private String token;
    private String role;
    private String username;
    private int userId;

    private UserSession() {
        if (AuthStorage.hasToken()) {
            this.token = AuthStorage.getToken();
            this.role = AuthStorage.getRole();
            this.username = AuthStorage.getUsername();
            this.userId = AuthStorage.getUserId();
        }
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void login(LoginResult result) {
        this.token = result.getToken();
        this.role = result.getRole();
        this.username = result.getUsername();
        this.userId = result.getUserId();
        AuthStorage.save(token, role, username, userId);
    }

    public void logout() {
        this.token = null;
        this.role = null;
        this.username = null;
        this.userId = 0;
        AuthStorage.clear();
    }

    public boolean isLoggedIn() { return token != null && !token.isEmpty(); }
    public String getToken() { return token; }
    public String getRole() { return role; }
    public String getUsername() { return username; }
    public int getUserId() { return userId; }

    /** Build JSON string for WebView localStorage injection */
    public String toJson() {
        try {
            return ApiClient.getMapper().writeValueAsString(
                    Map.of("token", token != null ? token : "",
                            "role", role != null ? role : "",
                            "username", username != null ? username : "",
                            "userId", userId));
        } catch (Exception e) {
            return "{}";
        }
    }
}
