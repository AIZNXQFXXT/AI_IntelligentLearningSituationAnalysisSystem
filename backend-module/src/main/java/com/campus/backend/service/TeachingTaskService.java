package com.campus.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.TeachingTask;
import com.campus.common.dto.TeachingTaskDTO;

import java.util.List;

public interface TeachingTaskService {
    TeachingTask create(TeachingTaskDTO dto);
    TeachingTask update(TeachingTaskDTO dto);
    void delete(Long id);
    TeachingTask findById(Long id);
    IPage<TeachingTask> pageList(int page, int size, String keyword);
    List<TeachingTask> findByTeacher(Long teacherId);
    List<TeachingTask> findByClass(Long classId);
}