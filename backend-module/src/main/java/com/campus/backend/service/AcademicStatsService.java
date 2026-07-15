package com.campus.backend.service;

import com.campus.common.vo.CourseSummaryVO;
import com.campus.common.vo.GradeSummaryVO;
import com.campus.common.vo.RiskDistributionVO;

import java.util.List;

public interface AcademicStatsService {
    List<GradeSummaryVO> getGradeSummary(String grade, Long courseId);
    List<CourseSummaryVO> getCourseSummary(String grade, Long courseId);
    List<RiskDistributionVO> getRiskDistribution(String grade, String groupBy);
}
