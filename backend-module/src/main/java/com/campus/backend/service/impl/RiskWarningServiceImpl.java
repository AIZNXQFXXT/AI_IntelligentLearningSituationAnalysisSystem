package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.ai.AiRequest;
import com.campus.backend.ai.AiServiceFactory;
import com.campus.backend.converter.RiskWarningConverter;
import com.campus.backend.entity.*;
import com.campus.backend.mapper.*;
import com.campus.backend.service.RiskWarningService;
import com.campus.common.constant.PromptTemplate;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import com.campus.common.vo.PageResult;
import com.campus.common.vo.RiskWarningVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskWarningServiceImpl implements RiskWarningService {

    private final RiskWarningMapper riskWarningMapper;
    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;
    private final ExamMapper examMapper;
    private final AiServiceFactory aiServiceFactory;
    private final RiskWarningConverter converter;

    @Override
    public void autoDetect(String semester, Long teacherId) {
        List<Student> students = studentMapper.selectList(null);

        for (Student student : students) {
            List<Score> scores = scoreMapper.selectList(
                    new LambdaQueryWrapper<Score>()
                            .eq(Score::getStudentId, student.getId())
            );

            if (scores.isEmpty()) continue;

            Map<Long, String> examSemesterMap = examMapper.selectList(null).stream()
                    .collect(Collectors.toMap(Exam::getId, Exam::getSemester));

            double avgScore = calculateAvgScore(scores, examSemesterMap, semester);
            boolean belowThreshold = avgScore < 60.0;
            List<String> decliningCourses = checkConsecutiveDecline(scores, examSemesterMap);

            String riskIndicators = buildRiskIndicators(scores, decliningCourses, avgScore);
            String initialRiskLevel = evaluateExpandedRisk(scores, belowThreshold, !decliningCourses.isEmpty());

            if ("NONE".equals(initialRiskLevel)) continue;

            String scoresText = scores.stream()
                    .map(s -> String.format("考试:%d 课程:%d 成绩:%.1f 排名:%d 缺考:%d 作弊:%d",
                            s.getExamId(), s.getCourseId(), s.getFinalScore(),
                            s.getRankClass(), s.getIsAbsent(), s.getIsCheat()))
                    .collect(Collectors.joining("\n"));

            String prompt = String.format(PromptTemplate.RISK_ANALYSIS_PROMPT,
                    student.getName(), "", riskIndicators, scoresText);

            AiRequest request = AiRequest.builder()
                    .prompt(prompt)
                    .model(aiServiceFactory.getActiveModel())
                    .callerId(teacherId)
                    .functionName("risk_analysis")
                    .promptTemplate("RISK_ANALYSIS_PROMPT")
                    .temperature(0.3)
                    .maxTokens(1024)
                    .build();

            var aiResult = aiServiceFactory.execute(request);

            RiskWarning warning = new RiskWarning();
            warning.setStudentId(student.getId());
            warning.setSemester(semester);
            String cleanedContent = com.campus.backend.ai.AiUtils.extractJsonContent(aiResult.getContent());
            warning.setAiAnalysis(cleanedContent);
            warning.setHandleStatus("PENDING");

            try {
                var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(cleanedContent);
                String aiLevel = json.has("riskLevel") ? json.get("riskLevel").asText() : null;
                String normalized = com.campus.backend.ai.AiUtils.normalizeRiskLevel(aiLevel);
                warning.setRiskLevel(normalized != null ? normalized : initialRiskLevel);
                warning.setRiskReason(json.has("riskReason") ? json.get("riskReason").asText() : "");
            } catch (Exception e) {
                warning.setRiskLevel(initialRiskLevel);
            }

            riskWarningMapper.insert(warning);
        }
    }

    @Override
    public PageResult<RiskWarningVO> pageList(int page, int size, String semester, String riskLevel, String handleStatus, String keyword) {
        LambdaQueryWrapper<RiskWarning> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskWarning::getIsDeleted, 0);
        if (semester != null && !semester.isEmpty()) {
            wrapper.eq(RiskWarning::getSemester, semester);
        }
        if (riskLevel != null) {
            wrapper.eq(RiskWarning::getRiskLevel, riskLevel);
        }
        if (handleStatus != null) {
            wrapper.eq(RiskWarning::getHandleStatus, handleStatus);
        }
        if (keyword != null && !keyword.isEmpty()) {
            List<Long> studentIds = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .and(w -> w.like(Student::getName, keyword)
                                    .or().like(Student::getStudentNo, keyword))
                            .select(Student::getId)
            ).stream().map(Student::getId).collect(Collectors.toList());
            if (studentIds.isEmpty()) {
                return PageResult.of(List.of(), 0, page, size);
            }
            wrapper.in(RiskWarning::getStudentId, studentIds);
        }
        wrapper.orderByDesc(RiskWarning::getRiskLevel)
                .orderByAsc(RiskWarning::getHandleStatus);
        Page<RiskWarning> result = riskWarningMapper.selectPage(new Page<>(page, size), wrapper);
        List<RiskWarningVO> records = result.getRecords().stream()
                .map(converter::toVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), page, size);
    }

    @Override
    public List<RiskWarningVO> exportList(String semester, String riskLevel, String handleStatus) {
        LambdaQueryWrapper<RiskWarning> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskWarning::getIsDeleted, 0);
        if (semester != null) wrapper.eq(RiskWarning::getSemester, semester);
        if (riskLevel != null) wrapper.eq(RiskWarning::getRiskLevel, riskLevel);
        if (handleStatus != null) wrapper.eq(RiskWarning::getHandleStatus, handleStatus);
        wrapper.orderByDesc(RiskWarning::getRiskLevel).orderByAsc(RiskWarning::getHandleStatus);
        return riskWarningMapper.selectList(wrapper).stream()
                .map(converter::toVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<RiskWarningVO> listByStudent(int page, int size, Long studentId, String semester, String riskLevel) {
        LambdaQueryWrapper<RiskWarning> wrapper = new LambdaQueryWrapper<RiskWarning>()
                .eq(RiskWarning::getStudentId, studentId);
        if (semester != null && !semester.isEmpty()) {
            wrapper.eq(RiskWarning::getSemester, semester);
        }
        if (riskLevel != null && !riskLevel.isEmpty()) {
            wrapper.eq(RiskWarning::getRiskLevel, riskLevel);
        }
        wrapper.orderByDesc(RiskWarning::getCreatedAt);
        Page<RiskWarning> result = riskWarningMapper.selectPage(new Page<>(page, size), wrapper);
        List<RiskWarningVO> records = result.getRecords().stream()
                .map(converter::toVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void handle(Long id, String remark, Long handlerId) {
        RiskWarning warning = riskWarningMapper.selectById(id);
        if (warning == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        warning.setHandleStatus("HANDLED");
        warning.setHandlerId(handlerId);
        warning.setHandleRemark(remark);
        warning.setHandleAt(LocalDateTime.now());
        riskWarningMapper.updateById(warning);
    }

    private String buildRiskIndicators(List<Score> scores, List<String> decliningCourses, double avgScore) {
        long failCount = scores.stream().filter(s -> s.getFinalScore() != null && (s.getFinalScore().compareTo(BigDecimal.valueOf(60)) < 0)).count();
        long absentCount = scores.stream().filter(s -> s.getIsAbsent() != null && s.getIsAbsent() == 1).count();
        long cheatCount = scores.stream().filter(s -> s.getIsCheat() != null && s.getIsCheat() == 1).count();

        return String.format("不及格科目数:%d 缺考次数:%d 作弊次数:%d 连续下滑科目数:%d 学期平均分:%.1f",
                failCount, absentCount, cheatCount, decliningCourses.size(), avgScore);
    }

    private String evaluateExpandedRisk(List<Score> scores, boolean belowThreshold, boolean hasDecline) {
        long failCount = scores.stream().filter(s -> s.getFinalScore() != null && (s.getFinalScore().compareTo(BigDecimal.valueOf(60)) < 0)).count();
        long absentCount = scores.stream().filter(s -> s.getIsAbsent() != null && s.getIsAbsent() == 1).count();

        int level = 0;
        if (failCount >= 3 || absentCount >= 2) level = 2;
        else if (failCount >= 1 || absentCount >= 1) level = 1;

        if (belowThreshold) level = Math.min(level + 1, 2);
        if (hasDecline) level = Math.min(level + 1, 2);

        if (level >= 2) return "HIGH";
        if (level == 1) return "MEDIUM";
        return "NONE";
    }

    private double calculateAvgScore(List<Score> scores, Map<Long, String> examSemesterMap, String currentSemester) {
        return scores.stream()
                .filter(s -> currentSemester.equals(examSemesterMap.get(s.getExamId())))
                .filter(s -> s.getFinalScore() != null)
                .mapToDouble(s -> s.getFinalScore().doubleValue())
                .average()
                .orElse(0.0);
    }

    private List<String> checkConsecutiveDecline(List<Score> scores, Map<Long, String> examSemesterMap) {
        return scores.stream()
                .filter(s -> s.getFinalScore() != null)
                .collect(Collectors.groupingBy(Score::getCourseId))
                .values().stream()
                .filter(list -> list.size() >= 3)
                .map(list -> {
                    List<Score> sorted = list.stream()
                            .sorted(Comparator.comparing(s -> examSemesterMap.getOrDefault(s.getExamId(), "")))
                            .toList();
                    int size = sorted.size();
                    BigDecimal s1 = sorted.get(size - 3).getFinalScore();
                    BigDecimal s2 = sorted.get(size - 2).getFinalScore();
                    BigDecimal s3 = sorted.get(size - 1).getFinalScore();
                    return (s1.compareTo(s2) > 0 && s2.compareTo(s3) > 0)
                            ? String.valueOf(sorted.get(size - 1).getCourseId())
                            : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}