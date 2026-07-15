package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Course;
import com.campus.backend.entity.RiskWarning;
import com.campus.backend.entity.Score;
import com.campus.backend.entity.Student;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.CourseMapper;
import com.campus.backend.mapper.RiskWarningMapper;
import com.campus.backend.mapper.ScoreMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.service.AcademicStatsService;
import com.campus.common.vo.CourseSummaryVO;
import com.campus.common.vo.GradeSummaryVO;
import com.campus.common.vo.RiskDistributionVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AcademicStatsServiceImpl implements AcademicStatsService {

    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;
    private final CourseMapper courseMapper;
    private final RiskWarningMapper riskWarningMapper;

    @Override
    public List<GradeSummaryVO> getGradeSummary(String grade, Long courseId) {
        List<Long> studentIds = getFilteredStudentIds(grade);
        if (studentIds != null && studentIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Score> scores = queryScores(studentIds, courseId);
        if (scores.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> studentClassMap = getStudentClassMap(scores);
        Map<Long, String> classGradeMap = getClassGradeMap(new ArrayList<>(studentClassMap.values()));

        Map<String, List<Score>> grouped = new HashMap<>();
        for (Score s : scores) {
            Long classId = studentClassMap.get(s.getStudentId());
            if (classId == null) continue;
            String g = classGradeMap.get(classId);
            if (g == null || g.isEmpty()) continue;
            grouped.computeIfAbsent(g, k -> new ArrayList<>()).add(s);
        }

        List<String> sortedGrades = new ArrayList<>(grouped.keySet());
        Collections.sort(sortedGrades);

        List<GradeSummaryVO> result = new ArrayList<>();
        for (String g : sortedGrades) {
            result.add(buildGradeSummary(g, grouped.get(g)));
        }
        return result;
    }

    @Override
    public List<CourseSummaryVO> getCourseSummary(String grade, Long courseId) {
        List<Long> studentIds = getFilteredStudentIds(grade);
        if (studentIds != null && studentIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Score> scores = queryScores(studentIds, courseId);
        if (scores.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = scores.stream().map(Score::getCourseId).distinct().toList();
        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                        .in(Course::getId, courseIds));
        Map<Long, String> courseNameMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, Course::getName));

        Map<Long, List<Score>> grouped = scores.stream()
                .collect(Collectors.groupingBy(Score::getCourseId));

        List<Long> sortedCourseIds = new ArrayList<>(grouped.keySet());
        Collections.sort(sortedCourseIds);

        List<CourseSummaryVO> result = new ArrayList<>();
        for (Long cid : sortedCourseIds) {
            result.add(buildCourseSummary(cid, courseNameMap.getOrDefault(cid, "未知"), grouped.get(cid)));
        }
        return result;
    }

    @Override
    public List<RiskDistributionVO> getRiskDistribution(String grade, String groupBy) {
        List<Long> studentIds = getFilteredStudentIds(grade);
        if (studentIds != null && studentIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<RiskWarning> wrapper = new LambdaQueryWrapper<RiskWarning>()
                .eq(RiskWarning::getIsDeleted, 0);
        if (studentIds != null) {
            wrapper.in(RiskWarning::getStudentId, studentIds);
        }
        List<RiskWarning> warnings = riskWarningMapper.selectList(wrapper);
        if (warnings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> warnStudentIds = warnings.stream()
                .map(RiskWarning::getStudentId).distinct().toList();

        if ("course".equals(groupBy)) {
            return groupRiskByCourse(warnStudentIds, warnings);
        }
        return groupRiskByGrade(warnStudentIds, warnings);
    }

    private List<RiskDistributionVO> groupRiskByGrade(List<Long> warnStudentIds, List<RiskWarning> warnings) {
        Map<Long, Student> studentMap = getStudentMap(warnStudentIds);
        Map<Long, String> classGradeMap = getClassGradeMap(
                studentMap.values().stream().map(Student::getClassId).distinct().toList());

        Map<String, RiskCounter> grouped = new HashMap<>();
        for (RiskWarning w : warnings) {
            Student stu = studentMap.get(w.getStudentId());
            if (stu == null) continue;
            String g = classGradeMap.get(stu.getClassId());
            if (g == null || g.isEmpty()) continue;
            grouped.computeIfAbsent(g, k -> new RiskCounter()).add(w.getRiskLevel());
        }

        return buildRiskDistribution("grade", grouped);
    }

    private List<RiskDistributionVO> groupRiskByCourse(List<Long> warnStudentIds, List<RiskWarning> warnings) {
        List<Score> scores = scoreMapper.selectList(
                new LambdaQueryWrapper<Score>()
                        .in(Score::getStudentId, warnStudentIds)
                        .eq(Score::getIsDeleted, 0));
        if (scores.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> courseIds = scores.stream().map(Score::getCourseId).distinct().toList();
        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().in(Course::getId, courseIds));
        Map<Long, String> courseNameMap = courses.stream()
                .collect(Collectors.toMap(Course::getId, Course::getName));

        Map<Long, List<Long>> courseStudentMap = scores.stream()
                .collect(Collectors.groupingBy(
                        Score::getCourseId,
                        Collectors.mapping(Score::getStudentId, Collectors.toList())));

        Map<String, RiskCounter> grouped = new HashMap<>();
        for (Map.Entry<Long, List<Long>> entry : courseStudentMap.entrySet()) {
            String courseName = courseNameMap.getOrDefault(entry.getKey(), "未知");
            RiskCounter counter = new RiskCounter();
            for (RiskWarning w : warnings) {
                if (entry.getValue().contains(w.getStudentId())) {
                    counter.add(w.getRiskLevel());
                }
            }
            if (counter.total > 0) {
                grouped.put(courseName, counter);
            }
        }

        return buildRiskDistribution("course", grouped);
    }

    private List<Long> getFilteredStudentIds(String grade) {
        if (grade == null || grade.isBlank()) {
            return null;
        }
        List<ClassInfo> classes = classMapper.selectList(
                new LambdaQueryWrapper<ClassInfo>()
                        .eq(ClassInfo::getGrade, grade));
        if (classes.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> classIds = classes.stream().map(ClassInfo::getId).toList();
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>()
                        .in(Student::getClassId, classIds));
        return students.stream().map(Student::getId).toList();
    }

    private List<Score> queryScores(List<Long> studentIds, Long courseId) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<Score>()
                .eq(Score::getIsDeleted, 0);

        if (studentIds != null) {
            if (studentIds.isEmpty()) {
                return Collections.emptyList();
            }
            wrapper.in(Score::getStudentId, studentIds);
        }
        if (courseId != null) {
            wrapper.eq(Score::getCourseId, courseId);
        }

        return scoreMapper.selectList(wrapper);
    }

    private Map<Long, Long> getStudentClassMap(List<Score> scores) {
        List<Long> studentIds = scores.stream().map(Score::getStudentId).distinct().toList();
        if (studentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>()
                        .in(Student::getId, studentIds));
        return students.stream()
                .collect(Collectors.toMap(Student::getId, Student::getClassId));
    }

    private Map<Long, String> getClassGradeMap(List<Long> classIds) {
        if (classIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ClassInfo> classInfos = classMapper.selectList(
                new LambdaQueryWrapper<ClassInfo>()
                        .in(ClassInfo::getId, classIds));
        return classInfos.stream()
                .filter(c -> c.getGrade() != null)
                .collect(Collectors.toMap(ClassInfo::getId, ClassInfo::getGrade));
    }

    private Map<Long, Student> getStudentMap(List<Long> studentIds) {
        if (studentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>()
                        .in(Student::getId, studentIds));
        return students.stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
    }

    private GradeSummaryVO buildGradeSummary(String grade, List<Score> scores) {
        GradeSummaryVO vo = new GradeSummaryVO();
        vo.setGrade(grade);

        List<Long> distinctStudents = scores.stream()
                .map(Score::getStudentId).distinct().toList();
        vo.setTotalStudents(distinctStudents.size());
        vo.setScoredStudents(scores.size());

        if (scores.isEmpty()) {
            return vo;
        }

        double sum = 0;
        int validCount = 0;
        int excellent = 0, good = 0, medium = 0, pass = 0, fail = 0;

        for (Score s : scores) {
            if (s.getFinalScore() == null) continue;
            double v = s.getFinalScore().doubleValue();
            sum += v;
            validCount++;

            if (v >= 90) excellent++;
            else if (v >= 80) good++;
            else if (v >= 70) medium++;
            else if (v >= 60) pass++;
            else fail++;
        }

        vo.setAvgScore(validCount > 0
                ? Math.round(sum / validCount * 100.0) / 100.0 : 0);

        int passed = validCount - fail;
        vo.setPassRate(validCount > 0
                ? Math.round(passed * 10000.0 / validCount) / 100.0 : 0);
        vo.setFailCount(fail);
        vo.setExcellentCount(excellent);
        vo.setGoodCount(good);
        vo.setMediumCount(medium);
        vo.setPassCount(pass);

        return vo;
    }

    private CourseSummaryVO buildCourseSummary(Long courseId, String courseName, List<Score> scores) {
        CourseSummaryVO vo = new CourseSummaryVO();
        vo.setCourseId(courseId);
        vo.setCourseName(courseName);

        List<Long> distinctStudents = scores.stream()
                .map(Score::getStudentId).distinct().toList();
        vo.setTotalStudents(distinctStudents.size());
        vo.setScoredStudents(scores.size());

        if (scores.isEmpty()) {
            return vo;
        }

        double sum = 0;
        int validCount = 0;
        int excellent = 0, good = 0, medium = 0, pass = 0, fail = 0;

        for (Score s : scores) {
            if (s.getFinalScore() == null) continue;
            double v = s.getFinalScore().doubleValue();
            sum += v;
            validCount++;

            if (v >= 90) excellent++;
            else if (v >= 80) good++;
            else if (v >= 70) medium++;
            else if (v >= 60) pass++;
            else fail++;
        }

        vo.setAvgScore(validCount > 0
                ? Math.round(sum / validCount * 100.0) / 100.0 : 0);

        int passed = validCount - fail;
        vo.setPassRate(validCount > 0
                ? Math.round(passed * 10000.0 / validCount) / 100.0 : 0);
        vo.setFailCount(fail);
        vo.setExcellentCount(excellent);
        vo.setGoodCount(good);
        vo.setMediumCount(medium);
        vo.setPassCount(pass);

        return vo;
    }

    private List<RiskDistributionVO> buildRiskDistribution(String dimension, Map<String, RiskCounter> grouped) {
        List<String> sortedKeys = new ArrayList<>(grouped.keySet());
        Collections.sort(sortedKeys);

        List<RiskDistributionVO> result = new ArrayList<>();
        for (String key : sortedKeys) {
            RiskCounter c = grouped.get(key);
            RiskDistributionVO vo = new RiskDistributionVO();
            vo.setDimension(dimension);
            vo.setDimensionValue(key);
            vo.setHighRiskCount(c.high);
            vo.setMediumRiskCount(c.medium);
            vo.setLowRiskCount(c.low);
            vo.setTotalCount(c.total);
            result.add(vo);
        }
        return result;
    }

    private static class RiskCounter {
        int high, medium, low, total;

        void add(String riskLevel) {
            if ("HIGH".equals(riskLevel)) high++;
            else if ("MEDIUM".equals(riskLevel)) medium++;
            else if ("LOW".equals(riskLevel)) low++;
            total++;
        }
    }
}
