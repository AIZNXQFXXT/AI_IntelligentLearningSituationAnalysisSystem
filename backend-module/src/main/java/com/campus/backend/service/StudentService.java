package com.campus.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.Student;
import com.campus.common.dto.StudentDTO;
import org.springframework.web.multipart.MultipartFile;

public interface StudentService {
    Student create(StudentDTO dto);
    Student update(StudentDTO dto);
    void delete(Long id);
    Student findById(Long id);
    IPage<Student> pageList(int page, int size, String keyword, Long classId, String role, Long userId);
    void batchImport(MultipartFile file);  // EasyExcel 批量导入
    void toggleStatus(Long id, Integer status);
}
