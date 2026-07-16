package com.campus.backend.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.service.ClassService;
import com.campus.backend.util.SecurityHelper;
import com.campus.common.dto.ClassDTO;
import com.campus.common.dto.report.ClassRow;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.ClassExportVO;
import com.campus.common.vo.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/my")
    public ApiResponse<PageResult<ClassInfo>> findMyClasses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        IPage<ClassInfo> result = classService.findMyClasses(page, size, keyword, userId);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/list")
    public ApiResponse<List<ClassInfo>> listClass(@RequestParam(defaultValue = "1") int classId) {
        return ApiResponse.success(classService.listAll());
    }

    @GetMapping("/export/excel")
    public void exportExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        List<ClassExportVO> list = classService.exportList();
        List<ClassRow> rows = list.stream().map(c -> new ClassRow(
                c.getGrade(), c.getClassName(), c.getHeadTeacherName(), c.getStudentCount()
        )).collect(Collectors.toList());
        String encoded = java.net.URLEncoder.encode("班级列表.xlsx", java.nio.charset.StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        EasyExcel.write(response.getOutputStream(), ClassRow.class).sheet("班级列表").doWrite(rows);
    }
}