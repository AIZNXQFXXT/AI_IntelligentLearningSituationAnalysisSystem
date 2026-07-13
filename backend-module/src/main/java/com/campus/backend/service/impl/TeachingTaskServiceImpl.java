package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.entity.Exam;
import com.campus.backend.entity.TeachingTask;
import com.campus.backend.mapper.TeachingTaskMapper;
import com.campus.backend.service.TeachingTaskService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class TeachingTaskServiceImpl implements TeachingTaskService {
    private final TeachingTaskMapper teachingTaskMapper;

    @Override
    @Transactional
    public TeachingTask create(Long teacherId, Long classId, Long courseId, String semester) {
        TeachingTask teachingTask = new TeachingTask(teacherId, classId, courseId, semester);
        teachingTask.setCreatedAt(LocalDateTime.now());
        teachingTask.setUpdatedAt(LocalDateTime.now());
        teachingTaskMapper.insert(teachingTask);
        return teachingTask;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        teachingTaskMapper.deleteById(id);
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
        ArrayList<TeachingTask> teachingTasks = new ArrayList<>();
        TeachingTask teachingTask;
        do {
            teachingTask = teachingTaskMapper.selectById(teacherId);
            teachingTasks.add(teachingTask);
        } while (teachingTask != null);
        return teachingTasks;
    }

    @Override
    public List<TeachingTask> findByClass(Long classId) {
        ArrayList<TeachingTask> teachingTasks = new ArrayList<>();
        TeachingTask teachingTask;
        do {
            teachingTask = teachingTaskMapper.selectById(classId);
            teachingTasks.add(teachingTask);
        } while (teachingTask != null);
        return teachingTasks;
    }
}
