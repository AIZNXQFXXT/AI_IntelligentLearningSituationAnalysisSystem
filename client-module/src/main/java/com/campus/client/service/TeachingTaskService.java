package com.campus.client.service;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.PageResult;
import com.campus.client.model.TeachingTask;
import com.fasterxml.jackson.core.type.TypeReference;

public class TeachingTaskService {
    public static PageResult<TeachingTask> getPage(int page, int size, String semester) throws Exception {
        String path = "/teaching-tasks?page=" + page + "&size=" + size;
        if (semester != null && !semester.isEmpty()) {
            path += "&semester=" + semester;
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<TeachingTask>>>() {});
    }

    public static Void create(TeachingTask task) throws Exception {
        return ApiClient.post("/teaching-tasks", task, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void update(int id, TeachingTask task) throws Exception {
        return ApiClient.put("/teaching-tasks/" + id, task, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void delete(int id) throws Exception {
        return ApiClient.delete("/teaching-tasks/" + id, new TypeReference<ApiResponse<Void>>() {});
    }
}
