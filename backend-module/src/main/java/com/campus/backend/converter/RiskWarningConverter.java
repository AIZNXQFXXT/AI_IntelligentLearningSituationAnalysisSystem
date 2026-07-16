package com.campus.backend.converter;

import com.campus.backend.entity.RiskWarning;
import com.campus.common.vo.RiskWarningVO;
import org.springframework.stereotype.Component;

@Component
public class RiskWarningConverter {

    public RiskWarningVO toVO(RiskWarning entity) {
        if (entity == null) return null;
        RiskWarningVO vo = new RiskWarningVO();
        vo.setId(entity.getId());
        vo.setStudentId(entity.getStudentId());
        vo.setSemester(entity.getSemester());
        vo.setRiskLevel(entity.getRiskLevel());
        vo.setRiskReason(entity.getRiskReason());
        vo.setAiAnalysis(entity.getAiAnalysis());
        vo.setHandleStatus(entity.getHandleStatus());
        vo.setHandleRemark(entity.getHandleRemark());
        vo.setHandleAt(entity.getHandleAt());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}