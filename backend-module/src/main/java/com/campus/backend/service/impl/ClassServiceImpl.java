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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
        String className = dto.getClassName();
        ClassInfo entity = classMapper.selectByClassName(className);
        if (entity != null) {
            if (entity.getIsDeleted() == 0) {
                throw new BusinessException(ErrorCode.CONFLICT.getCode(), "班级名称已存在");
            }
            entity.setIsDeleted(0);
            entity.setUpdatedAt(LocalDateTime.now());
            classMapper.updateByClassName(entity.getClassName());
        } else {
            entity = classConverter.toEntity(dto);
            entity.setStudentCount(0);
            entity.setCreatedAt(LocalDateTime.now());
            classMapper.insert(entity);
        }

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
        // 教学任务所带班级
        List<Long> taskClassIds = teachingTaskMapper.selectList(
                new LambdaQueryWrapper<TeachingTask>()
                        .eq(TeachingTask::getTeacherId, teacher.getId()))
                .stream().map(TeachingTask::getClassId).distinct().collect(Collectors.toList());
        // 班主任所带班级
        List<Long> headClassIds = classMapper.selectList(
                new LambdaQueryWrapper<ClassInfo>()
                        .eq(ClassInfo::getHeadTeacherId, teacher.getId()))
                .stream().map(ClassInfo::getId).distinct().collect(Collectors.toList());
        // 并集去重
        Set<Long> classIdSet = new LinkedHashSet<>();
        classIdSet.addAll(taskClassIds);
        classIdSet.addAll(headClassIds);
        if (classIdSet.isEmpty()) {
            return new Page<>(page, size);
        }
        Page<ClassInfo> p = new Page<>(page, size);
        LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ClassInfo::getId, classIdSet);
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