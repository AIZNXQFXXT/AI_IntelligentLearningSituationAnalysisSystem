package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.converter.StudentConverter;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Student;
import com.campus.backend.entity.User;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.security.PasswordEncoder;
import com.campus.backend.service.StudentService;
import com.campus.common.dto.StudentDTO;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final StudentConverter studentConverter;
    private final ClassMapper classMapper;

    @Override
    @Transactional
    public Student create(StudentDTO dto) {
        // 校验学号唯一
        Long count = studentMapper.selectCount(
                new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, dto.getStudentNo()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.STUDENT_NO_EXISTS);
        }
        // 创建 sys_user
        User user = new User();
        user.setUsername(dto.getUsername() != null ? dto.getUsername() : dto.getStudentNo());
        user.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        user.setRole("STUDENT");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        // 创建 student
        Student entity = studentConverter.toEntity(dto);
        entity.setUserId(user.getId());
        if (entity.getStatus() == null) entity.setStatus(1);
        studentMapper.insert(entity);
        // 更新班级人数
        Long classId = entity.getClassId(); // 获取学生所属的班级ID
        if (classId != null) {
            classMapper.update(null, new LambdaUpdateWrapper<ClassInfo>()
                    .eq(ClassInfo::getId, classId)
                    // 直接写 SQL 片段，让数据库自己 +1，防止并发问题
                    .setSql("student_count = student_count + 1"));
        }
        return entity;
    }

    @Override
    @Transactional
    public Student update(StudentDTO dto) {
        Student entity = studentConverter.toEntity(dto);
        studentMapper.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        studentMapper.deleteById(id);
    }

    @Override
    public Student findById(Long id) {
        return studentMapper.selectById(id);
    }

    @Override
    public IPage<Student> pageList(int page, int size, String keyword) {
        Page<Student> p = new Page<>(page, size);
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Student::getName, keyword)
                    .or().like(Student::getStudentNo, keyword);
        }
        return studentMapper.selectPage(p, wrapper);
    }

    @Override
    public void batchImport(MultipartFile file) {

    }

    @Override
    @Transactional
    public void toggleStatus(Long id, Integer status) {
        Student entity = studentMapper.selectById(id);
        entity.setStatus(status);
        studentMapper.updateById(entity);
    }
}
