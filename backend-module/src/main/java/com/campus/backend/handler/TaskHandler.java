package com.campus.backend.handler;

import java.util.Map;

public interface TaskHandler {
    String getType();
    Runnable createRunnable(Long taskId, String fileUrl, Map<String, String> params);
}
