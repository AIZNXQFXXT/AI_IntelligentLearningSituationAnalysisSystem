package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.Student;
import com.campus.backend.service.StudentService;
import com.campus.common.dto.StudentDTO;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@AllArgsConstructor
public class StudentController {
    private StudentService studentService;

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
            @RequestParam(required = false) String keyword) {
        IPage<Student> result = studentService.pageList(page, size, keyword);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/export/excel")
    public ApiResponse<Void> exportExcel() {
        // EasyExcel 导出逻辑（见 ExcelUtil）
        return ApiResponse.success();
    }
}
