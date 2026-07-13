package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.Exam;
import com.campus.backend.entity.TeachingTask;
import com.campus.backend.service.TeachingTaskService;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teaching-tasks")
@AllArgsConstructor
public class TeachingTaskController {
    private final TeachingTaskService teachingTaskService;

    @PostMapping
    public ApiResponse<TeachingTask> create(@RequestBody Long teacherId, @RequestBody Long classId, @RequestBody Long courseId, @RequestBody String semester) {
        return ApiResponse.success(teachingTaskService.create(teacherId, classId, courseId, semester));
    }

    @GetMapping
    public ApiResponse<PageResult<TeachingTask>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        IPage<TeachingTask> result = teachingTaskService.pageList(page, size, keyword);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

}
