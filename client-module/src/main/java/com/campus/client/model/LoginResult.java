package com.aicampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResult {
    @JsonProperty("token")
    private String token;
    @JsonProperty("role")
    private String role;
    @JsonProperty("username")
    private String username;
    @JsonProperty("userId")
    private int userId;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
