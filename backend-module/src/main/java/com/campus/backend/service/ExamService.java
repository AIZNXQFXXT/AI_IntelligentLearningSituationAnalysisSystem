package com.campus.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.Exam;
import com.campus.common.dto.ExamDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ExamService {
    Exam create(ExamDTO dto);
    Exam update(ExamDTO dto);
    void delete(Long id);
    Exam findById(Long id);
    IPage<Exam> pageList(int page, int size, String keyword);
    void batchImport(MultipartFile file);  // EasyExcel 批量导入
    void toggleArchive(Long id);
}
