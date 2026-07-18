package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.entity.User;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.security.PasswordEncoder;
import com.campus.backend.service.UserService;
import com.campus.common.dto.UserDTO;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public IPage<User> pageList(int page, int size, String role) {
        Page<User> p = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByAsc(User::getId);
        return userMapper.selectPage(p, wrapper);
    }

    @Override
    @Transactional
    public User create(UserDTO dto) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode("123456"));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.deleteById(user);
    }
}
