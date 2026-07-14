package com.campus.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.Score;
import com.campus.common.dto.ScoreDTO;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public interface ScoreService {
    Score create(ScoreDTO dto, Long enteredBy);
    Score updateScore(Long id, ScoreDTO dto, Long operatorId);  // 自动写 score_correction
    Score findById(Long id);
    IPage<Score> pageList(int page, int size, Long examId, Long courseId,
                          BigDecimal minScore, BigDecimal maxScore, Long classId);
    void updateStatus(Long id, String status);
    void batchImport(MultipartFile file, Long teacherId);  // 异步
}
