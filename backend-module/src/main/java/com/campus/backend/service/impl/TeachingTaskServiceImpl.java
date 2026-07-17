package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.converter.TeachingTaskConverter;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Course;
import com.campus.backend.entity.Teacher;
import com.campus.backend.entity.TeachingTask;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.CourseMapper;
import com.campus.backend.mapper.TeacherMapper;
import com.campus.backend.mapper.TeachingTaskMapper;
import com.campus.backend.service.TeachingTaskService;
import com.campus.common.dto.TeachingTaskDTO;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class TeachingTaskServiceImpl implements TeachingTaskService {
    private final TeachingTaskMapper teachingTaskMapper;
    private final TeachingTaskConverter teachingTaskConverter;
    private final TeacherMapper teacherMapper;
    private final ClassMapper classMapper;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public TeachingTask create(TeachingTaskDTO dto) {
        if (dto.getTeacherNo() != null) {
            Teacher t = teacherMapper.selectByTeacherNo(dto.getTeacherNo());
            if (t == null) throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "教师工号不存在");
            dto.setTeacherId(t.getId());
        }
        if (dto.getClassName() != null) {
            ClassInfo c = classMapper.selectByClassName(dto.getClassName());
            if (c == null) throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "班级不存在");
            dto.setClassId(c.getId());
        }
        if (dto.getCourseName() != null) {
            Course c = courseMapper.selectByName(dto.getCourseName());
            if (c == null) throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "课程不存在");
            dto.setCourseId(c.getId());
        }
        TeachingTask entity = teachingTaskConverter.toEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        teachingTaskMapper.insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public TeachingTask update(TeachingTaskDTO dto) {
        TeachingTask entity = teachingTaskConverter.toEntity(dto);
        entity.setUpdatedAt(LocalDateTime.now());
        teachingTaskMapper.updateById(entity);
        return entity;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        teachingTaskMapper.deleteById(id);
    }

    @Override
    public TeachingTask findById(Long id) {
        return teachingTaskMapper.selectById(id);
    }

    @Override
    public IPage<TeachingTask> pageList(int page, int size, String keyword) {
        Page<TeachingTask> p = new Page<>(page, size);
        LambdaQueryWrapper<TeachingTask> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(TeachingTask::getSemester, keyword);
        }
        wrapper.orderByAsc(TeachingTask::getId);
        return teachingTaskMapper.selectPage(p, wrapper);
    }

    @Override
    public List<TeachingTask> findByTeacher(Long teacherId) {
        LambdaQueryWrapper<TeachingTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachingTask::getTeacherId, teacherId);
        return teachingTaskMapper.selectList(wrapper);
    }

    @Override
    public List<TeachingTask> findByClass(Long classId) {
        LambdaQueryWrapper<TeachingTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachingTask::getClassId, classId);
        return teachingTaskMapper.selectList(wrapper);
    }
}
