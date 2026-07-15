package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.RiskWarning;
import org.apache.ibatis.annotations.Select;

public interface RiskWarningMapper extends BaseMapper<RiskWarning> {

    @Select("SELECT COUNT(*) FROM risk_warning WHERE is_deleted = 0")
    long countTotal();

    @Select("SELECT COUNT(*) FROM risk_warning WHERE is_deleted = 0 AND risk_level = 'HIGH'")
    long countHighRisk();

    @Select("SELECT COUNT(*) FROM risk_warning WHERE is_deleted = 0 AND risk_level = 'MEDIUM'")
    long countMediumRisk();

    @Select("SELECT COUNT(*) FROM risk_warning WHERE is_deleted = 0 AND risk_level = 'LOW'")
    long countLowRisk();

    @Select("SELECT COUNT(*) FROM risk_warning WHERE is_deleted = 0 AND handle_status = 'HANDLED'")
    long countHandled();

    @Select("SELECT COUNT(*) FROM risk_warning WHERE is_deleted = 0 AND (handle_status IS NULL OR handle_status != 'HANDLED')")
    long countUnhandled();
}
