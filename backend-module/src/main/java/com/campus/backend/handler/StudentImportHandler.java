package com.campus.backend.handler;

import com.campus.backend.async.StudentImportTask;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.service.StudentService;
import com.campus.backend.service.TaskService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StudentImportHandler implements TaskHandler {

    private final StudentService studentService;
    private final TaskService taskService;
    private final ClassMapper classMapper;

    public StudentImportHandler(StudentService studentService, TaskService taskService,
                                ClassMapper classMapper) {
        this.studentService = studentService;
        this.taskService = taskService;
        this.classMapper = classMapper;
    }

    @Override
    public String getType() {
        return "STUDENT_IMPORT";
    }

    @Override
    public Runnable createRunnable(Long taskId, String fileUrl, Map<String, String> params) {
        return new StudentImportTask(taskId, fileUrl, studentService, taskService, classMapper);
    }
}
