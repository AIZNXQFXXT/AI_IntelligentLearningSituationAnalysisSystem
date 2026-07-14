package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.converter.TeachingTaskConverter;
import com.campus.backend.entity.TeachingTask;
import com.campus.backend.mapper.TeachingTaskMapper;
import com.campus.backend.service.TeachingTaskService;
import com.campus.common.dto.TeachingTaskDTO;
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

    @Override
    @Transactional
    public TeachingTask create(TeachingTaskDTO dto) {
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
