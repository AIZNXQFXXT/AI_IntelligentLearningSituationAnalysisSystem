package com.campus.backend.handler;

import com.campus.backend.async.TeacherImportTask;
import com.campus.backend.service.TaskService;
import com.campus.backend.service.TeacherService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TeacherImportHandler implements TaskHandler {

    private final TeacherService teacherService;
    private final TaskService taskService;

    public TeacherImportHandler(TeacherService teacherService, TaskService taskService) {
        this.teacherService = teacherService;
        this.taskService = taskService;
    }

    @Override
    public String getType() {
        return "TEACHER_IMPORT";
    }

    @Override
    public Runnable createRunnable(Long taskId, String fileUrl, Map<String, String> params) {
        return new TeacherImportTask(taskId, fileUrl, teacherService, taskService);
    }
}
