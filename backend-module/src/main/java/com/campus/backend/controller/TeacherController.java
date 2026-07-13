package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.Teacher;
import com.campus.backend.service.TeacherService;
import com.campus.common.dto.TeacherDTO;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
@AllArgsConstructor
public class TeacherController {
    private final TeacherService teacherService;

    @PostMapping
    public ApiResponse<Teacher> create(@RequestBody TeacherDTO dto) {
        return ApiResponse.success(teacherService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<Teacher> update(@PathVariable Long id, @RequestBody TeacherDTO dto) {
        dto.setId(id);
        return ApiResponse.success(teacherService.update(dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<PageResult<Teacher>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        IPage<Teacher> result = teacherService.pageList(page, size, keyword);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/export/excel")
    public ApiResponse<Void> exportExcel() {
        // EasyExcel 导出逻辑（见 ExcelUtil）
        return ApiResponse.success();
    }
}
