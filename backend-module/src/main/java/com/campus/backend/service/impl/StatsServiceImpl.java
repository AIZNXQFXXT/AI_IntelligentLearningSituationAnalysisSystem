package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Course;
import com.campus.backend.entity.Exam;
import com.campus.backend.entity.Score;
import com.campus.backend.entity.Student;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.CourseMapper;
import com.campus.backend.mapper.ExamMapper;
import com.campus.backend.mapper.ScoreMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.service.StatsService;
import com.campus.common.vo.ClassStatsVO;
import com.campus.common.vo.CourseGradeVO;
import com.campus.common.vo.GpaRankingVO;
import com.campus.common.vo.GradePointVO;
import com.campus.common.vo.RankingItemVO;
import com.campus.common.vo.ScoreDistributionVO;
import com.campus.common.vo.TrendItemVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;
    private final ExamMapper examMapper;
    private final CourseMapper courseMapper;

    @Override
    public ClassStatsVO getClassStats(Long classId, Long examId, Long courseId) {
        ClassInfo classInfo = classMapper.selectById(classId);
        if (classInfo == null) {
            return null;
        }

        List<Long> studentIds = getStudentIdsByClass(classId);
        List<Score> scores = queryScores(studentIds, examId, courseId);

        ClassStatsVO vo = new ClassStatsVO();
        vo.setClassId(classId);
        vo.setClassName(classInfo.getClassName());
        vo.setTotalStudents(studentIds.size());
        vo.setScoredStudents(scores.size());

        if (scores.isEmpty()) {
            return vo;
        }

        List<BigDecimal> allScores = new ArrayList<>();
        double sum = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        int excellent = 0, good = 0, medium = 0, pass = 0, fail = 0;

        for (Score s : scores) {
            BigDecimal fs = s.getFinalScore();
            if (fs == null) continue;
            double v = fs.doubleValue();
            allScores.add(fs);
            sum += v;
            max = Math.max(max, v);
            min = Math.min(min, v);

            if (v >= 90) excellent++;
            else if (v >= 80) good++;
            else if (v >= 70) medium++;
            else if (v >= 60) pass++;
            else fail++;
        }

        vo.setAvgScore(Math.round(sum / allScores.size() * 100.0) / 100.0);
        vo.setMaxScore(max == Double.MIN_VALUE ? 0 : max);
        vo.setMinScore(min == Double.MAX_VALUE ? 0 : min);

        int passed = allScores.size() - fail;
        vo.setPassRate(Math.round(passed * 10000.0 / allScores.size()) / 100.0);

        vo.setExcellentCount(excellent);
        vo.setGoodCount(good);
        vo.setMediumCount(medium);
        vo.setPassCount(pass);
        vo.setFailCount(fail);

        Collections.sort(allScores);
        int size = allScores.size();
        if (size % 2 == 0) {
            double m1 = allScores.get(size / 2 - 1).doubleValue();
            double m2 = allScores.get(size / 2).doubleValue();
            vo.setMedianScore(Math.round((m1 + m2) / 2 * 100.0) / 100.0);
        } else {
            vo.setMedianScore(allScores.get(size / 2).doubleValue());
        }

        return vo;
    }

    @Override
    public List<ScoreDistributionVO> getScoreDistribution(Long examId, Long courseId, Long classId) {
        List<Long> studentIds = classId != null ? getStudentIdsByClass(classId) : null;
        List<Score> scores = queryScores(studentIds, examId, courseId);

        if (scores.isEmpty()) {
            return Collections.emptyList();
        }

        int total = 0, excellent = 0, good = 0, medium = 0, pass = 0, fail = 0;
        for (Score s : scores) {
            BigDecimal fs = s.getFinalScore();
            if (fs == null) continue;
            total++;
            double v = fs.doubleValue();
            if (v >= 90) excellent++;
            else if (v >= 80) good++;
            else if (v >= 70) medium++;
            else if (v >= 60) pass++;
            else fail++;
        }

        List<ScoreDistributionVO> list = new ArrayList<>();
        addDistributionItem(list, "90-100", excellent, total);
        addDistributionItem(list, "80-89", good, total);
        addDistributionItem(list, "70-79", medium, total);
        addDistributionItem(list, "60-69", pass, total);
        addDistributionItem(list, "<60", fail, total);
        return list;
    }

    @Override
    public List<RankingItemVO> getRanking(Long examId, Long courseId, Long classId) {
        if (examId == null || courseId == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<Score>()
                .eq(Score::getExamId, examId)
                .eq(Score::getCourseId, courseId)
                .eq(Score::getIsDeleted, 0)
                .orderByDesc(Score::getFinalScore);

        List<Score> scores = scoreMapper.selectList(wrapper);
        if (scores.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> studentIds = scores.stream().map(Score::getStudentId).toList();
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>().in(Student::getId, studentIds));
        Map<Long, Student> studentMap = students.stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        List<Long> classIds = students.stream().map(Student::getClassId).distinct().toList();
        List<ClassInfo> classInfos = classMapper.selectList(
                new LambdaQueryWrapper<ClassInfo>().in(ClassInfo::getId, classIds));
        Map<Long, String> classNameMap = classInfos.stream()
                .collect(Collectors.toMap(ClassInfo::getId, ClassInfo::getClassName));

        List<RankingItemVO> result = new ArrayList<>();
        int rank = 1;
        for (Score s : scores) {
            Student stu = studentMap.get(s.getStudentId());
            if (stu == null) continue;
            if (classId != null && !classId.equals(stu.getClassId())) continue;

            RankingItemVO item = new RankingItemVO();
            item.setRank(rank++);
            item.setStudentId(stu.getId());
            item.setStudentNo(stu.getStudentNo());
            item.setStudentName(stu.getName());
            item.setClassId(stu.getClassId());
            item.setClassName(classNameMap.getOrDefault(stu.getClassId(), ""));
            item.setFinalScore(s.getFinalScore());
            result.add(item);
        }

        return result;
    }

    @Override
    public List<TrendItemVO> getTrend(Long classId, Long courseId) {
        List<Long> studentIds = classId != null ? getStudentIdsByClass(classId) : null;
        List<Score> scores = queryScores(studentIds, null, courseId);

        if (scores.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> examIds = scores.stream().map(Score::getExamId).distinct().toList();
        List<Exam> exams = examMapper.selectList(
                new LambdaQueryWrapper<Exam>().in(Exam::getId, examIds));
        Map<Long, String> examSemesterMap = exams.stream()
                .collect(Collectors.toMap(Exam::getId, Exam::getSemester));

        Map<String, List<BigDecimal>> semesterScores = new HashMap<>();
        for (Score s : scores) {
            String semester = examSemesterMap.get(s.getExamId());
            if (semester == null || semester.isEmpty()) continue;
            if (s.getFinalScore() == null) continue;
            semesterScores.computeIfAbsent(semester, k -> new ArrayList<>()).add(s.getFinalScore());
        }

        List<TrendItemVO> result = new ArrayList<>();
        List<String> sortedSemesters = new ArrayList<>(semesterScores.keySet());
        Collections.sort(sortedSemesters);

        for (String semester : sortedSemesters) {
            List<BigDecimal> ss = semesterScores.get(semester);
            double sum = 0;
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;
            int passed = 0;

            for (BigDecimal fs : ss) {
                double v = fs.doubleValue();
                sum += v;
                max = Math.max(max, v);
                min = Math.min(min, v);
                if (v >= 60) passed++;
            }

            TrendItemVO item = new TrendItemVO();
            item.setSemester(semester);
            item.setStudentCount(ss.size());
            item.setAvgScore(Math.round(sum / ss.size() * 100.0) / 100.0);
            item.setPassRate(Math.round(passed * 10000.0 / ss.size()) / 100.0);
            item.setMaxScore(max == Double.MIN_VALUE ? 0 : max);
            item.setMinScore(min == Double.MAX_VALUE ? 0 : min);
            result.add(item);
        }

        return result;
    }

    @Override
    public List<GradePointVO> getGradePoints(Long classId, Long courseId) {
        List<Long> studentIds = getStudentIdsByClass(classId);
        if (studentIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Score> scores = queryScores(studentIds, null, courseId);
        if (scores.isEmpty()) {
            return Collections.emptyList();
        }

        // 课程学分映射
        List<Long> courseIds = scores.stream().map(Score::getCourseId).distinct().toList();
        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().in(Course::getId, courseIds));
        Map<Long, Double> creditMap = courses.stream()
                .collect(Collectors.toMap(Course::getId,
                        c -> c.getCredit() == null ? 0.0 : c.getCredit()));

        // 学生信息映射
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>().in(Student::getId, studentIds));
        Map<Long, Student> studentMap = students.stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        // 按学生分组成绩
        Map<Long, List<Score>> byStudent = scores.stream()
                .collect(Collectors.groupingBy(Score::getStudentId));

        List<GradePointVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<Score>> entry : byStudent.entrySet()) {
            Long sid = entry.getKey();
            Student stu = studentMap.get(sid);
            if (stu == null) continue;

            List<Score> stuScores = entry.getValue();
            double gpa;
            int courseCount;
            if (courseId != null) {
                // 单科绩点：取最近一条（id 最大，随创建时间递增）
                Score latest = stuScores.stream()
                        .max(Comparator.comparing(Score::getId))
                        .orElse(null);
                if (latest == null || latest.getFinalScore() == null) continue;
                gpa = bracketGpa(latest.getFinalScore().doubleValue());
                courseCount = 1;
            } else {
                // 全课程学分加权 GPA
                List<Double> sList = new ArrayList<>();
                List<Double> cList = new ArrayList<>();
                for (Score s : stuScores) {
                    if (s.getFinalScore() == null) continue;
                    sList.add(s.getFinalScore().doubleValue());
                    cList.add(creditMap.getOrDefault(s.getCourseId(), 0.0));
                }
                if (sList.isEmpty()) continue;
                gpa = weightedGpa(sList, cList);
                courseCount = sList.size();
            }

            GradePointVO vo = new GradePointVO();
            vo.setStudentId(sid);
            vo.setStudentNo(stu.getStudentNo());
            vo.setStudentName(stu.getName());
            vo.setGpa(gpa);
            vo.setCourseCount(courseCount);
            result.add(vo);
        }

        result.sort((a, b) -> Double.compare(b.getGpa(), a.getGpa()));
        return result;
    }

    @Override
    public List<CourseGradeVO> getCourseGrades(Long classId) {
        return scoreMapper.selectCourseGradeStats(classId);
    }

    @Override
    public List<GpaRankingVO> getGpaRanking(String grade) {
        if (grade == null || grade.isEmpty()) return Collections.emptyList();

        List<ClassInfo> classInfos = classMapper.selectList(
                new LambdaQueryWrapper<ClassInfo>().eq(ClassInfo::getGrade, grade).eq(ClassInfo::getIsDeleted, 0));
        if (classInfos.isEmpty()) return Collections.emptyList();
        List<Long> classIds = classInfos.stream().map(ClassInfo::getId).toList();

        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>().in(Student::getClassId, classIds).eq(Student::getStatus, 1));
        if (students.isEmpty()) return Collections.emptyList();
        Map<Long, Student> studentMap = students.stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        Map<Long, Long> studentClassIdMap = students.stream()
                .collect(Collectors.toMap(Student::getId, Student::getClassId));
        Map<Long, String> classNameMap = classInfos.stream()
                .collect(Collectors.toMap(ClassInfo::getId, ClassInfo::getClassName));

        List<Long> studentIds = students.stream().map(Student::getId).toList();
        List<Score> scores = scoreMapper.selectList(
                new LambdaQueryWrapper<Score>()
                        .in(Score::getStudentId, studentIds)
                        .eq(Score::getIsDeleted, 0)
                        .isNotNull(Score::getFinalScore));

        List<Long> courseIds = scores.stream().map(Score::getCourseId).distinct().toList();
        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().in(Course::getId, courseIds));
        Map<Long, Double> creditMap = courses.stream()
                .collect(Collectors.toMap(Course::getId,
                        c -> c.getCredit() == null ? 0.0 : c.getCredit()));

        Map<Long, List<Score>> byStudent = scores.stream()
                .collect(Collectors.groupingBy(Score::getStudentId));

        List<GpaRankingVO> ranking = new ArrayList<>();
        for (Map.Entry<Long, List<Score>> entry : byStudent.entrySet()) {
            Long sid = entry.getKey();
            Student stu = studentMap.get(sid);
            if (stu == null) continue;

            List<Double> sList = new ArrayList<>();
            List<Double> cList = new ArrayList<>();
            for (Score s : entry.getValue()) {
                if (s.getFinalScore() == null) continue;
                sList.add(s.getFinalScore().doubleValue());
                cList.add(creditMap.getOrDefault(s.getCourseId(), 0.0));
            }
            if (sList.isEmpty()) continue;

            double gpa = weightedGpa(sList, cList);
            GpaRankingVO vo = new GpaRankingVO();
            vo.setStudentId(sid);
            vo.setStudentNo(stu.getStudentNo());
            vo.setStudentName(stu.getName());
            vo.setClassName(classNameMap.get(studentClassIdMap.get(sid)));
            vo.setGpa(gpa);
            vo.setCourseCount(sList.size());
            ranking.add(vo);
        }

        ranking.sort((a, b) -> Double.compare(b.getGpa(), a.getGpa()));
        int rank = 1;
        for (GpaRankingVO vo : ranking) {
            vo.setRank(rank++);
        }
        return ranking;
    }

    private List<Long> getStudentIdsByClass(Long classId) {
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getClassId, classId)
                        .eq(Student::getStatus, 1));
        return students.stream().map(Student::getId).toList();
    }

    private List<Score> queryScores(List<Long> studentIds, Long examId, Long courseId) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<Score>()
                .eq(Score::getIsDeleted, 0);

        if (studentIds != null && !studentIds.isEmpty()) {
            wrapper.in(Score::getStudentId, studentIds);
        }
        if (studentIds != null && studentIds.isEmpty()) {
            return Collections.emptyList();
        }

        if (examId != null) {
            wrapper.eq(Score::getExamId, examId);
        }
        if (courseId != null) {
            wrapper.eq(Score::getCourseId, courseId);
        }

        return scoreMapper.selectList(wrapper);
    }

    private void addDistributionItem(List<ScoreDistributionVO> list, String label, int count, int total) {
        double pct = Math.round(count * 10000.0 / total) / 100.0;
        list.add(new ScoreDistributionVO(label, count, pct));
    }

    /** 单科绩点（五分制）。不及格返回 0。 */
    static double bracketGpa(double score) {
        if (score >= 90) return (score - 90) * 0.1 + 4.0;
        if (score >= 80) return (score - 80) * 0.1 + 3.0;
        if (score >= 70) return (score - 70) * 0.1 + 2.0;
        if (score >= 60) return (score - 60) * 0.1 + 1.0;
        return 0.0;
    }

    /** 学分加权 GPA = Σ(绩点×学分) / Σ学分，保留 2 位小数。总学分为 0 返回 0。 */
    static double weightedGpa(List<Double> scores, List<Double> credits) {
        double sumGpCredit = 0.0;
        double sumCredit = 0.0;
        for (int i = 0; i < scores.size(); i++) {
            sumGpCredit += bracketGpa(scores.get(i)) * credits.get(i);
            sumCredit += credits.get(i);
        }
        if (sumCredit == 0) return 0.0;
        return Math.round(sumGpCredit / sumCredit * 100.0) / 100.0;
    }
}
