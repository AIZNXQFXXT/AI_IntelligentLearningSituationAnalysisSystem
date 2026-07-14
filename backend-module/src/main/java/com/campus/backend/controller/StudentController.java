package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.Student;
import com.campus.backend.service.StudentService;
import com.campus.common.dto.StudentDTO;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@AllArgsConstructor
public class StudentController {
    private final StudentService studentService;

    @PostMapping
    public ApiResponse<Student> create(@RequestBody StudentDTO dto) {
        return ApiResponse.success(studentService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<Student> update(@PathVariable Long id, @RequestBody StudentDTO dto) {
        dto.setId(id);
        return ApiResponse.success(studentService.update(dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<PageResult<Student>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long classId,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        IPage<Student> result = studentService.pageList(page, size, keyword, classId, role, userId);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        studentService.toggleStatus(id, status);
        return ApiResponse.success();
    }

    @GetMapping("/export/excel")
    public ApiResponse<Void> exportExcel() {
        return ApiResponse.success();
    }
}
