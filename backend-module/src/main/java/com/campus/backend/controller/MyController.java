package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Course;
import com.campus.backend.entity.Student;
import com.campus.backend.entity.TeachingTask;
import com.campus.backend.entity.User;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.CourseMapper;
import com.campus.backend.mapper.ScoreMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.mapper.TeachingTaskMapper;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.service.CommentService;
import com.campus.backend.service.DiagnosisService;
import com.campus.backend.service.RiskWarningService;
import com.campus.backend.service.SuggestionService;
import com.campus.backend.util.SecurityHelper;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.CommentVO;
import com.campus.common.vo.DiagnosisVO;
import com.campus.common.vo.PageResult;
import com.campus.common.vo.RiskWarningVO;
import com.campus.common.vo.ScoreArchiveVO;
import com.campus.common.vo.ScoreRadarVO;
import com.campus.common.vo.ScoreTrendVO;
import com.campus.common.vo.StudentProfileVO;
import com.campus.common.vo.SuggestionVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/my")
@AllArgsConstructor
public class MyController {

    private final StudentMapper studentMapper;
    private final UserMapper userMapper;
    private final ClassMapper classMapper;
    private final ScoreMapper scoreMapper;
    private final TeachingTaskMapper teachingTaskMapper;
    private final CourseMapper courseMapper;
    private final DiagnosisService diagnosisService;
    private final SuggestionService suggestionService;
    private final CommentService commentService;
    private final RiskWarningService riskWarningService;

    private Student resolveStudent(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Student student = studentMapper.selectByUserId(userId);
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "学生档案不存在");
        }
        return student;
    }

    @GetMapping("/profile")
    public ApiResponse<StudentProfileVO> profile(HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "STUDENT");
        Long userId = (Long) request.getAttribute("userId");
        Student student = studentMapper.selectByUserId(userId);
        if (student == null) {
            return ApiResponse.error(ErrorCode.NOT_FOUND.getCode(), "学生档案不存在");
        }
        User user = userMapper.selectById(userId);
        ClassInfo classInfo = student.getClassId() != null ? classMapper.selectById(student.getClassId()) : null;

        StudentProfileVO vo = new StudentProfileVO();
        vo.setStudentNo(student.getStudentNo());
        vo.setName(student.getName());
        vo.setGender(student.getGender());
        vo.setClassName(classInfo != null ? classInfo.getClassName() : null);
        vo.setEnrollYear(student.getEnrollYear());
        vo.setAvatar(user != null ? user.getAvatar() : null);
        vo.setPhone(user != null ? user.getPhone() : null);
        vo.setGuardianPhone(student.getGuardianPhone());
        vo.setStatus(student.getStatus());
        return ApiResponse.success(vo);
    }

    @GetMapping("/courses")
    public ApiResponse<List<Course>> courses(
            @RequestParam(required = false) String semester,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "STUDENT");
        Student student = resolveStudent(request);
        List<TeachingTask> tasks = teachingTaskMapper.selectList(
                new LambdaQueryWrapper<TeachingTask>()
                        .eq(TeachingTask::getClassId, student.getClassId())
                        .eq(semester != null && !semester.isEmpty(), TeachingTask::getSemester, semester));
        if (tasks.isEmpty()) {
            return ApiResponse.success(List.of());
        }
        List<Long> courseIds = tasks.stream().map(TeachingTask::getCourseId).distinct().toList();
        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        return ApiResponse.success(courses);
    }

    @GetMapping("/scores")
    public ApiResponse<PageResult<ScoreArchiveVO>> scores(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String semester,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "STUDENT");
        Student student = resolveStudent(request);
        var result = scoreMapper.selectByStudentPage(
                new Page<>(page, size), student.getId(), semester);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/scores/trend")
    public ApiResponse<List<ScoreTrendVO>> scoreTrend(HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "STUDENT");
        Student student = resolveStudent(request);
        return ApiResponse.success(scoreMapper.selectTrend(student.getId()));
    }

    @GetMapping("/scores/radar")
    public ApiResponse<List<ScoreRadarVO>> scoreRadar(
            @RequestParam(required = false) String semester,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "STUDENT");
        Student student = resolveStudent(request);
        return ApiResponse.success(scoreMapper.selectRadar(student.getId(), semester));
    }

    @GetMapping("/diagnosis")
    public ApiResponse<PageResult<DiagnosisVO>> diagnosis(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "STUDENT");
        Student student = resolveStudent(request);
        return ApiResponse.success(diagnosisService.pageHistory(page, size, student.getId()));
    }

    @GetMapping("/suggestions")
    public ApiResponse<SuggestionVO> suggestions(
            @RequestParam(required = false) String semester,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "STUDENT");
        Long userId = (Long) request.getAttribute("userId");
        Student student = studentMapper.selectByUserId(userId);
        if (student == null) {
            return ApiResponse.error(ErrorCode.NOT_FOUND.getCode(), "学生档案不存在");
        }
        return ApiResponse.success(suggestionService.getSuggestion(student.getId(), semester, userId));
    }

    @GetMapping("/comments")
    public ApiResponse<PageResult<CommentVO>> comments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String semester,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "STUDENT");
        Student student = resolveStudent(request);
        return ApiResponse.success(commentService.listByStudent(page, size, student.getId(), semester));
    }

    @GetMapping("/warnings")
    public ApiResponse<PageResult<RiskWarningVO>> warnings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "STUDENT");
        Student student = resolveStudent(request);
        return ApiResponse.success(riskWarningService.listByStudent(page, size, student.getId()));
    }
}
