package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.backend.entity.TeachingTask;
import com.campus.backend.mapper.*;
import com.campus.backend.service.DashboardService;
import com.campus.common.vo.DashboardVO;
import com.campus.common.vo.RiskSummaryVO;
import com.campus.common.vo.SchoolOverviewVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ClassMapper classMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final ExamMapper examMapper;
    private final ScoreMapper scoreMapper;
    private final RiskWarningMapper riskWarningMapper;

    @Override
    public DashboardVO getAdminDashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setClassCount(classMapper.selectCount(null));
        vo.setTeacherCount(teacherMapper.selectCount(null));
        vo.setStudentCount(studentMapper.selectCount(null));
        vo.setCourseCount(courseMapper.selectCount(null));
        return vo;
    }

    @Override
    public SchoolOverviewVO getSchoolOverview() {
        SchoolOverviewVO vo = new SchoolOverviewVO();
        vo.setTotalStudents(studentMapper.selectCount(null));
        vo.setTotalClasses(classMapper.selectCount(null));
        vo.setTotalCourses(courseMapper.selectCount(null));
        vo.setTotalExams(examMapper.selectCount(null));

        long total = scoreMapper.countTotal();
        if (total > 0) {
            vo.setAvgScore(scoreMapper.avgFinalScore());
            long passed = scoreMapper.countPassed();
            vo.setPassRate(Math.round(passed * 10000.0 / total) / 100.0);
            vo.setFailRate(Math.round((total - passed) * 10000.0 / total) / 100.0);
            vo.setExcellentCount(scoreMapper.countExcellent());
            vo.setGoodCount(scoreMapper.countGood());
            vo.setMediumCount(scoreMapper.countMedium());
            vo.setPassCount(scoreMapper.countPass());
            vo.setFailCount(scoreMapper.countFail());
        }
        return vo;
    }

    @Override
    public RiskSummaryVO getRiskSummary() {
        RiskSummaryVO vo = new RiskSummaryVO();
        vo.setTotalWarnings(riskWarningMapper.countTotal());
        vo.setHighRiskCount(riskWarningMapper.countHighRisk());
        vo.setMediumRiskCount(riskWarningMapper.countMediumRisk());
        vo.setLowRiskCount(riskWarningMapper.countLowRisk());
        vo.setHandledCount(riskWarningMapper.countHandled());
        vo.setUnhandledCount(riskWarningMapper.countUnhandled());
        return vo;
    }
}
