package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.Exam;
import com.campus.backend.entity.Student;
import com.campus.backend.service.ExamService;
import com.campus.common.dto.ExamDTO;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams")
@AllArgsConstructor
public class ExamController {
    private final ExamService examService;

    @PostMapping
    public ApiResponse<Exam> create(@RequestBody ExamDTO dto) {
        return ApiResponse.success(examService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<Exam> update(@PathVariable Long id, @RequestBody ExamDTO dto) {
        dto.setId(id);
        return ApiResponse.success(examService.update(dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        examService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<PageResult<Exam>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        IPage<Exam> result = examService.pageList(page, size, keyword);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @PatchMapping("/{id}/archive")
    public ApiResponse<Student> updateStatus(@PathVariable Long id) {
        examService.toggleArchive(id);
        return ApiResponse.success();
    }

    @GetMapping("/export/excel")
    public ApiResponse<Void> exportExcel() {
        // EasyExcel 导出逻辑（见 ExcelUtil）
        return ApiResponse.success();
    }
}
