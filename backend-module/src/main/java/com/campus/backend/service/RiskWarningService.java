package com.campus.backend.service;

import com.campus.common.vo.PageResult;
import com.campus.common.vo.RiskWarningVO;

import java.util.List;

public interface RiskWarningService {
    void autoDetect(String semester, Long teacherId);
    PageResult<RiskWarningVO> pageList(int page, int size, String semester, String riskLevel, String handleStatus);
    List<RiskWarningVO> exportList(String semester, String riskLevel, String handleStatus);
    void handle(Long id, String remark, Long handlerId);
    PageResult<RiskWarningVO> listByStudent(int page, int size, Long studentId, String semester, String riskLevel);
}
