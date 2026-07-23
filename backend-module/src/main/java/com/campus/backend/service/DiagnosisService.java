package com.campus.backend.service;

import com.campus.common.dto.AIDiagnosisDTO;
import com.campus.common.vo.DiagnosisVO;
import com.campus.common.vo.PageResult;

public interface DiagnosisService {
    DiagnosisVO diagnose(AIDiagnosisDTO dto, Long teacherId);
    PageResult<DiagnosisVO> pageHistory(int page, int size, Long studentId, String semester);

    /**
     * 回填：遍历已有诊断记录，把 riskLevel 同步到 risk_warning 表。
     * @return 处理的诊断条数
     */
    int backfillRiskWarnings();
}
