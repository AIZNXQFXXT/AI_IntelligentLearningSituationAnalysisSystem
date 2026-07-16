package com.campus.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.Score;
import com.campus.common.dto.ScoreDTO;
import com.campus.common.vo.ScoreArchiveVO;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ScoreService {
    Score create(ScoreDTO dto, Long enteredBy);
    Score updateScore(Long id, ScoreDTO dto, Long operatorId);  // 自动写 score_correction
    Score findById(Long id);
    IPage<Score> pageList(int page, int size, Long examId, Long courseId,
                          BigDecimal minScore, BigDecimal maxScore, Long classId);
    IPage<ScoreArchiveVO> archiveOverview(int page, int size);
    void updateStatus(Long id, String status);
    void batchImport(MultipartFile file, Long teacherId);
    List<ScoreArchiveVO> exportList(Long examId, Long courseId, Long classId);
}
