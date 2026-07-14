package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.backend.entity.TeachingTask;
import com.campus.backend.mapper.*;
import com.campus.backend.service.DashboardService;
import com.campus.common.vo.DashboardVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ClassMapper classMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;

    @Override
    public DashboardVO getAdminDashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setClassCount(classMapper.selectCount(null));
        vo.setTeacherCount(teacherMapper.selectCount(null));
        vo.setStudentCount(studentMapper.selectCount(null));
        vo.setCourseCount(courseMapper.selectCount(null));
        return vo;
    }
}
