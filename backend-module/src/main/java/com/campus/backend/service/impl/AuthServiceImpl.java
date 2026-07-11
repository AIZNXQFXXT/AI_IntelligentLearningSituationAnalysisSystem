package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.backend.entity.User;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.security.JwtUtil;
import com.campus.backend.security.PasswordEncoder;
import com.campus.backend.service.AuthService;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String login(String username, String password) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getIsDeleted, 0));
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return jwtUtil.generateAccessToken(user.getId(), user.getRole());
    }

    @Override
    public User getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return user;
    }
}