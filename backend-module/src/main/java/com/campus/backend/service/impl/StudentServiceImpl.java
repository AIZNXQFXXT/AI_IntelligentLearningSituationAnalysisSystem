package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.converter.StudentConverter;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Student;
import com.campus.backend.entity.Teacher;
import com.campus.backend.entity.TeachingTask;
import com.campus.backend.entity.User;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.mapper.TeacherMapper;
import com.campus.backend.mapper.TeachingTaskMapper;
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
import java.util.List;

@Service
@AllArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final StudentConverter studentConverter;
    private final ClassMapper classMapper;
    private final TeacherMapper teacherMapper;
    private final TeachingTaskMapper teachingTaskMapper;

    @Override
    @Transactional
    public Student create(StudentDTO dto) {
        // 解析 className+grade → classId
        if (dto.getClassName() != null) {
            ClassInfo c = classMapper.selectByClassNameAndGrade(dto.getClassName(), dto.getGrade());
            if (c == null) throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "班级不存在");
            dto.setClassId(c.getId());
        }

        // 检查是否有软删除的学生记录（含已删除）
        Student existing = studentMapper.selectByStudentNoIncludeDeleted(dto.getStudentNo());
        if (existing != null) {
            // 恢复 student 记录
            existing.setIsDeleted(0);
            existing.setName(dto.getName());
            existing.setGender(dto.getGender());
            existing.setClassId(dto.getClassId());
            existing.setEnrollYear(dto.getEnrollYear());
            existing.setPhone(dto.getPhone());
            existing.setGuardianPhone(dto.getGuardianPhone());
            existing.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
            existing.setUpdatedAt(LocalDateTime.now());
            studentMapper.recoverByStudentNo(dto.getStudentNo());

            // 恢复关联的 User（如果也处于软删除状态）
            User user = userMapper.selectById(existing.getUserId());
            if (user != null && user.getIsDeleted() == 1) {
                user.setIsDeleted(0);
                user.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
                user.setStatus(1);
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.recoverById(user.getId());
            }
            return existing;
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
        entity.setUpdatedAt(LocalDateTime.now());
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
    public Student findByStudentNo(String studentNo) {
        return studentMapper.selectOne(
            new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, studentNo));
    }

    @Override
    public IPage<Student> pageList(int page, int size, String keyword, Long classId, String role, Long userId) {
        Page<Student> p = new Page<>(page, size);
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Student::getName, keyword)
                    .or().like(Student::getStudentNo, keyword));
        }
        if (classId != null) {
            wrapper.eq(Student::getClassId, classId);
        } else if ("TEACHER".equals(role) && userId != null) {
            Teacher teacher = teacherMapper.selectOne(
                    new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, userId));
            if (teacher != null) {
                List<TeachingTask> tasks = teachingTaskMapper.selectList(
                        new LambdaQueryWrapper<TeachingTask>().eq(TeachingTask::getTeacherId, teacher.getId()));
                List<Long> classIds = tasks.stream().map(TeachingTask::getClassId).distinct().toList();
                if (!classIds.isEmpty()) {
                    wrapper.in(Student::getClassId, classIds);
                }
            }
        }
        wrapper.orderByAsc(Student::getId);
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
