package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Exam;
import com.campus.backend.entity.Score;
import com.campus.backend.entity.Student;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.ExamMapper;
import com.campus.backend.mapper.ScoreMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.service.StatsService;
import com.campus.common.vo.ClassStatsVO;
import com.campus.common.vo.RankingItemVO;
import com.campus.common.vo.ScoreDistributionVO;
import com.campus.common.vo.TrendItemVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
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
}
