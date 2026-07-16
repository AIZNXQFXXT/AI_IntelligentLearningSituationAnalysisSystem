package com.campus.common.constant;

public class RedisKeyConstant {

    private RedisKeyConstant() {}

    public static final String TOKEN_PREFIX = "token:";
    public static final String AI_RATE_PREFIX = "ai:daily:";
    public static final String REFRESH_TOKEN_PREFIX = "refresh:";
    public static final String DASHBOARD_CACHE = "dashboard:";
    public static final String STATS_CACHE = "stats:";

    public static String tokenKey(String token) {
        return TOKEN_PREFIX + token;
    }

    public static String aiRateKey(Long userId) {
        return AI_RATE_PREFIX + userId;
    }

    public static String dashboardKey(String role, Long userId) {
        return DASHBOARD_CACHE + role + ":" + userId;
    }
}