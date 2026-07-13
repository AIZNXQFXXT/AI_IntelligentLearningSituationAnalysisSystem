package com.campus.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.Course;
import com.campus.common.dto.CourseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourseService {
    Course create(CourseDTO dto);
    Course update(CourseDTO dto);
    void delete(Long id);
    Course findById(Long id);
    IPage<Course> pageList(int page, int size, String keyword);
    void batchImport(MultipartFile file);  // EasyExcel 批量导入
    void toggleStatus(Long id, Integer status);
    List<Course> listAll();
}
