package com.campus.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.TeachingTask;

import java.util.List;

public interface TeachingTaskService {
    TeachingTask create(Long teacherId, Long classId, Long courseId, String semester);
    void delete(Long id);
    IPage<TeachingTask> pageList(int page, int size, String keyword);
    List<TeachingTask> findByTeacher(Long teacherId);
    List<TeachingTask> findByClass(Long classId);
}