package com.campus.backend.service;

import com.campus.backend.entity.TaskRecord;

public interface TaskService {
    TaskRecord createTask(String taskType, String params);
    TaskRecord getProgress(Long taskId);
    TaskRecord getResult(Long taskId);
    void updateProgress(Long taskId, int progress, int current, int total);
    void complete(Long taskId, String resultJson);
    void fail(Long taskId, String error);
}
