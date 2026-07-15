package com.campus.backend.handler;

import com.campus.backend.async.ScoreImportTask;
import com.campus.backend.service.ScoreService;
import com.campus.backend.service.StudentService;
import com.campus.backend.service.TaskService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ScoreImportHandler implements TaskHandler {

    private final ScoreService scoreService;
    private final TaskService taskService;
    private final StudentService studentService;

    public ScoreImportHandler(ScoreService scoreService, TaskService taskService,
                              StudentService studentService) {
        this.scoreService = scoreService;
        this.taskService = taskService;
        this.studentService = studentService;
    }

    @Override
    public String getType() {
        return "SCORE_IMPORT";
    }

    @Override
    public Runnable createRunnable(Long taskId, String fileUrl, Map<String, String> params) {
        Long examId = Long.valueOf(params.get("examId"));
        Long courseId = Long.valueOf(params.get("courseId"));
        Long teacherId = Long.valueOf(params.get("teacherId"));
        return new ScoreImportTask(taskId, fileUrl, teacherId, examId, courseId,
                scoreService, taskService, studentService);
    }
}
