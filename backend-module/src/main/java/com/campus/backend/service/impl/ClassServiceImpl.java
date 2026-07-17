package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Student;
import com.campus.backend.entity.Teacher;
import com.campus.backend.entity.TeachingTask;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.mapper.TeacherMapper;
import com.campus.backend.mapper.TeachingTaskMapper;
import com.campus.backend.service.ClassService;
import com.campus.backend.converter.ClassConverter;
import com.campus.common.dto.ClassDTO;
import com.campus.common.vo.ClassExportVO;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ClassServiceImpl implements ClassService {
    private final ClassMapper classMapper;
    private final ClassConverter classConverter;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final TeachingTaskMapper teachingTaskMapper;

    @Override
    @Transactional
    public ClassInfo create(ClassDTO dto) {
        if (dto.getTeacherNo() != null) {
            Teacher t = teacherMapper.selectByTeacherNo(dto.getTeacherNo());
            if (t == null) throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "教师工号不存在");
            dto.setHeadTeacherId(t.getId());
        }
        ClassInfo entity = classConverter.toEntity(dto);
        entity.setStudentCount(0);
        entity.setCreatedAt(LocalDateTime.now());
        classMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public ClassInfo update(ClassDTO dto) {
        ClassInfo entity = classConverter.toEntity(dto);
        entity.setUpdatedAt(LocalDateTime.now());
        classMapper.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 检查班级是否有学生
        Long count = studentMapper.selectCount(new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, id));
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        classMapper.deleteById(id);
    }

    @Override
    public ClassInfo findById(Long id) {
        return classMapper.selectById(id);
    }

    @Override
    public IPage<ClassInfo> pageList(int page, int size, String keyword) {
        Page<ClassInfo> p = new Page<>(page, size);
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ClassInfo::getClassName, keyword)
                    .or().like(ClassInfo::getGrade, keyword);
        }
        wrapper.orderByAsc(ClassInfo::getId);
        return classMapper.selectPage(p, wrapper);
    }

    @Override
    public IPage<ClassInfo> findMyClasses(int page, int size, String keyword, Long userId) {
        Teacher teacher = teacherMapper.selectOne(
                new LambdaQueryWrapper<Teacher>().eq(Teacher::getUserId, userId));
        if (teacher == null) {
            return new Page<>(page, size);
        }
        List<Long> classIds = teachingTaskMapper.selectList(
                new LambdaQueryWrapper<TeachingTask>()
                        .eq(TeachingTask::getTeacherId, teacher.getId()))
                .stream().map(TeachingTask::getClassId).distinct().collect(Collectors.toList());
        if (classIds.isEmpty()) {
            return new Page<>(page, size);
        }
        Page<ClassInfo> p = new Page<>(page, size);
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ClassInfo::getId, classIds);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ClassInfo::getClassName, keyword)
                    .or().like(ClassInfo::getGrade, keyword));
        }
        wrapper.orderByAsc(ClassInfo::getId);
        return classMapper.selectPage(p, wrapper);
    }

    @Override
    public List<ClassInfo> listAll() {
        return classMapper.selectList(null);
    }

    @Override
    public List<ClassExportVO> exportList() {
        return classMapper.selectExportList();
    }
}