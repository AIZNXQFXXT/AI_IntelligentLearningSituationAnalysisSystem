package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.Course;
import com.campus.backend.service.CourseService;
import com.campus.common.dto.CourseDTO;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @PostMapping
    public ApiResponse<Course> create(@RequestBody CourseDTO dto) {
        return ApiResponse.success(courseService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<Course> update(@PathVariable Long id, @RequestBody CourseDTO dto) {
        dto.setId(id);
        return ApiResponse.success(courseService.update(dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ApiResponse.success();
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        courseService.toggleStatus(id, status);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<PageResult<Course>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        IPage<Course> result = courseService.pageList(page, size, keyword);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/list")
    public ApiResponse<List<Course>> listClass(@RequestParam(defaultValue = "1") int courseId) {
        return ApiResponse.success(courseService.listAll());
    }

    @GetMapping("/export/excel")
    public ApiResponse<Void> exportExcel() {
        // EasyExcel 导出逻辑（见 ExcelUtil）
        return ApiResponse.success();
    }
}
