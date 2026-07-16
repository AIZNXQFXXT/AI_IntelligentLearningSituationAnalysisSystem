package com.campus.backend.service;

import com.campus.common.vo.PageResult;
import com.campus.common.vo.RiskWarningVO;

public interface RiskWarningService {
    void autoDetect(String semester, Long teacherId);
    PageResult<RiskWarningVO> pageList(int page, int size, String riskLevel, String handleStatus);
    void handle(Long id, String remark, Long handlerId);
}
