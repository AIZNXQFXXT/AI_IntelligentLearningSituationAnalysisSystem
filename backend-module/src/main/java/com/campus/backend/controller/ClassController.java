package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.service.ClassService;
import com.campus.common.dto.ClassDTO;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@AllArgsConstructor
public class ClassController {
    private final ClassService classService;

    @PostMapping
    public ApiResponse<ClassInfo> create(@RequestBody ClassDTO dto) {
        return ApiResponse.success(classService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<ClassInfo> update(@PathVariable Long id, @RequestBody ClassDTO dto) {
        dto.setId(id);
        return ApiResponse.success(classService.update(dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        classService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<PageResult<ClassInfo>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        IPage<ClassInfo> result = classService.pageList(page, size, keyword);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/list")
    public ApiResponse<List<ClassInfo>> listClass(@RequestParam(defaultValue = "1") int classId) {
        return ApiResponse.success(classService.listAll());
    }

    @GetMapping("/export/excel")
    public ApiResponse<Void> exportExcel() {
        // EasyExcel 导出逻辑（见 ExcelUtil）
        return ApiResponse.success();
    }
}