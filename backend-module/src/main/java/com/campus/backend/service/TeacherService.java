package com.campus.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.Teacher;
import com.campus.common.dto.TeacherDTO;
import org.springframework.web.multipart.MultipartFile;

public interface TeacherService {
    Teacher create(TeacherDTO dto);
    Teacher update(TeacherDTO dto);
    void delete(Long id);
    Teacher findById(Long id);
    IPage<Teacher> pageList(int page, int size, String keyword);
    void batchImport(MultipartFile file);  // EasyExcel 批量导入
    void toggleStatus(Long id, Integer status);
}
