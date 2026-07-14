package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.TeachingTask;
import com.campus.backend.service.TeachingTaskService;
import com.campus.common.dto.TeachingTaskDTO;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teaching-tasks")
@AllArgsConstructor
public class TeachingTaskController {
    private final TeachingTaskService teachingTaskService;

    @PostMapping
    public ApiResponse<TeachingTask> create(@RequestBody TeachingTaskDTO dto) {
        return ApiResponse.success(teachingTaskService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<TeachingTask> update(@PathVariable Long id, @RequestBody TeachingTaskDTO dto) {
        dto.setId(id);
        return ApiResponse.success(teachingTaskService.update(dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        teachingTaskService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping("/{id}")
    public ApiResponse<TeachingTask> findById(@PathVariable Long id) {
        return ApiResponse.success(teachingTaskService.findById(id));
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

    @GetMapping("/teacher/{teacherId}")
    public ApiResponse<List<TeachingTask>> findByTeacher(@PathVariable Long teacherId) {
        return ApiResponse.success(teachingTaskService.findByTeacher(teacherId));
    }

    @GetMapping("/class/{classId}")
    public ApiResponse<List<TeachingTask>> findByClass(@PathVariable Long classId) {
        return ApiResponse.success(teachingTaskService.findByClass(classId));
    }
}
