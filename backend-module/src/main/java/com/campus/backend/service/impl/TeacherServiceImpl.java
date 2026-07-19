package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.converter.TeacherConverter;
import com.campus.backend.entity.Teacher;
import com.campus.backend.entity.User;
import com.campus.backend.mapper.TeacherMapper;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.security.PasswordEncoder;
import com.campus.backend.service.TeacherService;
import com.campus.common.dto.TeacherDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class TeacherServiceImpl implements TeacherService {
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final TeacherMapper teacherMapper;
    private final TeacherConverter teacherConverter;

    @Override
    @Transactional
    public Teacher create(TeacherDTO dto) {
        // 检查是否有软删除的教师记录（含已删除）
        Teacher existing = teacherMapper.selectByTeacherNoIncludeDeleted(dto.getTeacherNo());
        if (existing != null) {
            // 恢复 teacher 记录
            existing.setIsDeleted(0);
            existing.setName(dto.getName());
            existing.setTitle(dto.getTitle());
            existing.setSubject(dto.getSubject());
            existing.setEducation(dto.getEducation());
            existing.setDepartment(dto.getDepartment());
            existing.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
            existing.setUpdatedAt(LocalDateTime.now());
            teacherMapper.recoverByTeacherNo(dto.getTeacherNo());

            // 恢复关联的 User（如果也处于软删除状态）
            String username = dto.getUsername() != null ? dto.getUsername() : dto.getTeacherNo();
            User user = userMapper.selectByUsernameIncludeDeleted(username);
            if (user != null && user.getIsDeleted() == 1) {
                user.setIsDeleted(0);
                user.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
                user.setStatus(1);
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.recoverById(user.getId());
            }
            return existing;
        }

        // 1. 创建 sys_user（登录账号）
        User user = new User();
        user.setUsername(dto.getUsername() != null ? dto.getUsername() : dto.getTeacherNo());
        user.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        user.setRole("TEACHER");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        // 2. 创建 teacher（关联 user_id）
        Teacher entity = teacherConverter.toEntity(dto);
        entity.setUserId(user.getId());
        entity.setCreatedAt(LocalDateTime.now());
        teacherMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public Teacher update(TeacherDTO dto) {
        Teacher entity = teacherConverter.toEntity(dto);
        entity.setUpdatedAt(LocalDateTime.now());
        teacherMapper.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        teacherMapper.deleteById(id);
    }

    @Override
    public Teacher findById(Long id) {
        return teacherMapper.selectById(id);
    }

    @Override
    public IPage<Teacher> pageList(int page, int size, String keyword) {
        Page<Teacher> p = new Page<>(page, size);
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Teacher::getName, keyword)
                    .or().like(Teacher::getTeacherNo, keyword)
                    .or().like(Teacher::getDepartment, keyword)
                    .or().like(Teacher::getSubject, keyword);
        }
        wrapper.orderByAsc(Teacher::getId);
        return teacherMapper.selectPage(p, wrapper);
    }

    @Override
    public void batchImport(MultipartFile file) {

    }

    @Override
    @Transactional
    public void toggleStatus(Long id, Integer status) {
        Teacher entity = teacherMapper.selectById(id);
        entity.setStatus(status);
        teacherMapper.updateById(entity);
    }
}
