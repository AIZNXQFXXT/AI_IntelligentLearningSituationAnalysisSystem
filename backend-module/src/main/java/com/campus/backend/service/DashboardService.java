package com.campus.backend.service;

import com.campus.common.vo.DashboardVO;
import com.campus.common.vo.RiskSummaryVO;
import com.campus.common.vo.SchoolOverviewVO;

public interface DashboardService {
    DashboardVO getAdminDashboard();
    SchoolOverviewVO getSchoolOverview();
    RiskSummaryVO getRiskSummary();
}