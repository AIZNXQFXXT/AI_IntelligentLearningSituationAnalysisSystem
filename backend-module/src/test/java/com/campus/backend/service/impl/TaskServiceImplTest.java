package com.campus.backend.service.impl;

import com.campus.backend.entity.TaskRecord;
import com.campus.backend.mapper.TaskRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRecordMapper taskRecordMapper;

    @Captor
    private ArgumentCaptor<TaskRecord> recordCaptor;

    @Test
    void createTask_shouldCreatePendingTask() {
        TaskServiceImpl service = new TaskServiceImpl(taskRecordMapper);

        when(taskRecordMapper.insert(any())).thenAnswer(i -> {
            TaskRecord r = i.getArgument(0);
            r.setId(1L);
            return 1;
        });

        TaskRecord result = service.createTask("SCORE_IMPORT", null);

        assertNotNull(result);
        assertEquals("SCORE_IMPORT", result.getTaskType());
        assertEquals("PENDING", result.getStatus());
        assertEquals(Integer.valueOf(0), result.getProgress());
        assertEquals(1L, result.getId());
    }

    @Test
    void getProgress_shouldReturnTaskWithProgress() {
        TaskServiceImpl service = new TaskServiceImpl(taskRecordMapper);

        TaskRecord expected = new TaskRecord();
        expected.setId(1L);
        expected.setProgress(50);
        expected.setStatus("PROCESSING");

        when(taskRecordMapper.selectById(1L)).thenReturn(expected);

        TaskRecord result = service.getProgress(1L);

        assertEquals(50, result.getProgress());
        assertEquals("PROCESSING", result.getStatus());
    }

    @Test
    void getResult_shouldReturnTaskResult() {
        TaskServiceImpl service = new TaskServiceImpl(taskRecordMapper);

        TaskRecord expected = new TaskRecord();
        expected.setId(1L);
        expected.setStatus("COMPLETED");
        expected.setResultJson("{\"success\":10}");

        when(taskRecordMapper.selectById(1L)).thenReturn(expected);

        TaskRecord result = service.getResult(1L);

        assertEquals("COMPLETED", result.getStatus());
        assertEquals("{\"success\":10}", result.getResultJson());
    }

    @Test
    void updateProgress_shouldUpdateFields() {
        TaskServiceImpl service = new TaskServiceImpl(taskRecordMapper);

        TaskRecord existing = new TaskRecord();
        existing.setId(1L);

        when(taskRecordMapper.selectById(1L)).thenReturn(existing);

        service.updateProgress(1L, 50, 25, 50);

        verify(taskRecordMapper).updateById(recordCaptor.capture());
        TaskRecord updated = recordCaptor.getValue();
        assertEquals(Integer.valueOf(50), updated.getProgress());
        assertEquals(Integer.valueOf(25), updated.getCurrentCount());
        assertEquals(Integer.valueOf(50), updated.getTotalCount());
    }

    @Test
    void complete_shouldSetCompletedStatus() {
        TaskServiceImpl service = new TaskServiceImpl(taskRecordMapper);

        TaskRecord existing = new TaskRecord();
        existing.setId(1L);

        when(taskRecordMapper.selectById(1L)).thenReturn(existing);

        service.complete(1L, "{\"success\":10}");

        verify(taskRecordMapper).updateById(recordCaptor.capture());
        TaskRecord updated = recordCaptor.getValue();
        assertEquals("COMPLETED", updated.getStatus());
        assertEquals("{\"success\":10}", updated.getResultJson());
    }

    @Test
    void fail_shouldSetFailedStatus() {
        TaskServiceImpl service = new TaskServiceImpl(taskRecordMapper);

        TaskRecord existing = new TaskRecord();
        existing.setId(1L);

        when(taskRecordMapper.selectById(1L)).thenReturn(existing);

        service.fail(1L, "File format error");

        verify(taskRecordMapper).updateById(recordCaptor.capture());
        TaskRecord updated = recordCaptor.getValue();
        assertEquals("FAILED", updated.getStatus());
        assertEquals("File format error", updated.getErrorMessage());
    }
}
