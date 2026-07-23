package com.campus.backend.security;

import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    public JwtAuthInterceptor(JwtUtil jwtUtil, StringRedisTemplate redisTemplate) {
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (path.equals("/api/auth/login") || path.equals("/api/auth/refresh") || path.equals("/api/health")) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);
        if (redisTemplate.hasKey("blacklist:" + token)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        try {
            Long userId = jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            return true;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }
}