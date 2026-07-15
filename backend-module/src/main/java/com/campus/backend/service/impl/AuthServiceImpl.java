package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.backend.entity.User;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.security.JwtUtil;
import com.campus.backend.security.PasswordEncoder;
import com.campus.backend.service.AuthService;
import com.campus.common.dto.LoginDTO;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, dto.getUsername())
                        .eq(User::getIsDeleted, 0));
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("role", user.getRole());
        result.put("username", user.getUsername());
        result.put("userId", user.getId());
        return result;
    }

    @Override
    public User getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    @Override
    public void logout(Long userId, String token) {
        try {
            Claims claims = jwtUtil.parseToken(token);
            long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set("blacklist:" + token, "1", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public Map<String, Object> refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtUtil.parseToken(refreshToken);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Long userId = Long.parseLong(claims.getSubject());
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());
        Map<String, Object> result = new HashMap<>();
        result.put("token", newAccessToken);
        result.put("refreshToken", newRefreshToken);
        return result;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, Map<String, Object> updates) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (updates.containsKey("avatar")) {
            user.setAvatar((String) updates.get("avatar"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }
}