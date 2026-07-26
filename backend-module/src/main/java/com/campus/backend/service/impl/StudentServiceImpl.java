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
            if (existing.getIsDeleted() == 0) {
                throw new BusinessException(ErrorCode.CONFLICT.getCode(), "学号已存在");
            }
            // 记录 DB 中真实的旧 classId（软删除时已对旧班级 -1）
            Long oldClassId = existing.getClassId();
            Long newClassId = dto.getClassId();

            // 恢复 student 记录：全字段更新（含 class_id），保证 DB 与 DTO 一致
            existing.setIsDeleted(0);
            existing.setName(dto.getName());
            existing.setGender(dto.getGender());
            existing.setClassId(newClassId);
            existing.setEnrollYear(dto.getEnrollYear());
            existing.setPhone(dto.getPhone());
            existing.setGuardianPhone(dto.getGuardianPhone());
            existing.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
            existing.setUpdatedAt(LocalDateTime.now());
            studentMapper.recoverByStudentNo(existing.getStudentNo());

            // 恢复关联的 User（如果也处于软删除状态）
            String username = dto.getUsername() != null ? dto.getUsername() : dto.getStudentNo();
            User user = userMapper.selectByUsernameIncludeDeleted(username);
            if (user != null && user.getIsDeleted() == 1) {
                user.setIsDeleted(0);
                user.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
                user.setStatus(1);
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.recoverById(user.getId());
            }

            // 修正班级人数：删除时旧班级已 -1，恢复时撤销（旧班级 +1）；
            // 若班级变更，新班级要 +1；旧==新时只 +1 一次
            if (oldClassId != null) {
                classMapper.update(null, new LambdaUpdateWrapper<ClassInfo>()
                        .eq(ClassInfo::getId, oldClassId)
                        .setSql("student_count = student_count + 1"));
            }
            if (newClassId != null && !newClassId.equals(oldClassId)) {
                classMapper.update(null, new LambdaUpdateWrapper<ClassInfo>()
                        .eq(ClassInfo::getId, newClassId)
                        .setSql("student_count = student_count + 1"));
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
        Student entity = studentMapper.selectById(id);
        Long classId = entity.getClassId(); // 获取学生所属的班级ID
        if (classId != null) {
            classMapper.update(null, new LambdaUpdateWrapper<ClassInfo>()
                    .eq(ClassInfo::getId, classId)
                    // 直接写 SQL 片段，让数据库自己 -1，防止并发问题
                    .setSql("student_count = student_count - 1"));
        }
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
                // 教学任务所带班级
                List<Long> taskClassIds = teachingTaskMapper.selectList(
                        new LambdaQueryWrapper<TeachingTask>().eq(TeachingTask::getTeacherId, teacher.getId()))
                        .stream().map(TeachingTask::getClassId).distinct().toList();
                // 班主任所带班级
                List<Long> headClassIds = classMapper.selectList(
                        new LambdaQueryWrapper<ClassInfo>().eq(ClassInfo::getHeadTeacherId, teacher.getId()))
                        .stream().map(ClassInfo::getId).distinct().toList();
                // 并集去重
                java.util.Set<Long> classIds = new java.util.LinkedHashSet<>();
                classIds.addAll(taskClassIds);
                classIds.addAll(headClassIds);
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
