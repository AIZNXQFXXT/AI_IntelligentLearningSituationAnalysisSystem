package com.campus.backend.service;

import com.campus.common.dto.AIDiagnosisDTO;
import com.campus.common.vo.DiagnosisVO;
import com.campus.common.vo.PageResult;

public interface DiagnosisService {
    DiagnosisVO diagnose(AIDiagnosisDTO dto, Long teacherId);
    PageResult<DiagnosisVO> pageHistory(int page, int size, Long studentId);
}
