package com.campus.backend.service.impl;

import com.campus.backend.entity.TaskRecord;
import com.campus.backend.mapper.TaskRecordMapper;
import com.campus.backend.service.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRecordMapper taskRecordMapper;

    @Override
    public TaskRecord createTask(String taskType, String params) {
        TaskRecord record = new TaskRecord();
        record.setTaskType(taskType);
        record.setStatus("PENDING");
        record.setProgress(0);
        record.setCurrentCount(0);
        record.setTotalCount(0);
        taskRecordMapper.insert(record);
        return record;
    }

    @Override
    public TaskRecord getProgress(Long taskId) {
        return taskRecordMapper.selectById(taskId);
    }

    @Override
    public TaskRecord getResult(Long taskId) {
        return taskRecordMapper.selectById(taskId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateProgress(Long taskId, int progress, int current, int total) {
        TaskRecord record = taskRecordMapper.selectById(taskId);
        if (record != null) {
            record.setProgress(progress);
            record.setCurrentCount(current);
            record.setTotalCount(total);
            taskRecordMapper.updateById(record);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long taskId, String resultJson) {
        TaskRecord record = taskRecordMapper.selectById(taskId);
        if (record != null) {
            record.setStatus("COMPLETED");
            record.setResultJson(resultJson);
            record.setProgress(100);
            taskRecordMapper.updateById(record);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long taskId, String error) {
        TaskRecord record = taskRecordMapper.selectById(taskId);
        if (record != null) {
            record.setStatus("FAILED");
            record.setErrorMessage(error);
            taskRecordMapper.updateById(record);
        }
    }
}
