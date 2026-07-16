package com.campus.backend.handler;

import com.campus.backend.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExportTaskHandler implements TaskHandler {

    private final TaskService taskService;

    public ExportTaskHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String getType() {
        return "EXPORT";
    }

    @Override
    public Runnable createRunnable(Long taskId, String fileUrl, Map<String, String> params) {
        return () -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String msg = "Async export not yet implemented. Report type: " + params.get("reportType");
                taskService.complete(taskId, mapper.writeValueAsString(Map.of("message", msg)));
            } catch (Throwable e) {
                try {
                    taskService.fail(taskId, e.getMessage());
                } catch (Throwable ignored) {
                }
            }
        };
    }
}
