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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskWarningServiceImpl implements RiskWarningService {

    private final RiskWarningMapper riskWarningMapper;
    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;
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

            String riskIndicators = buildRiskIndicators(scores);
            String initialRiskLevel = evaluateInitialRisk(scores);

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
                    .model("deepseek-chat")
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
            warning.setAiAnalysis(aiResult.getContent());
            warning.setHandleStatus("UNHANDLED");

            try {
                var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(aiResult.getContent());
                warning.setRiskLevel(json.has("riskLevel") ? json.get("riskLevel").asText() : initialRiskLevel);
                warning.setRiskReason(json.has("riskReason") ? json.get("riskReason").asText() : "");
            } catch (Exception e) {
                warning.setRiskLevel(initialRiskLevel);
            }

            riskWarningMapper.insert(warning);
        }
    }

    @Override
    public PageResult<RiskWarningVO> pageList(int page, int size, String riskLevel, String handleStatus) {
        LambdaQueryWrapper<RiskWarning> wrapper = new LambdaQueryWrapper<>();
        if (riskLevel != null) {
            wrapper.eq(RiskWarning::getRiskLevel, riskLevel);
        }
        if (handleStatus != null) {
            wrapper.eq(RiskWarning::getHandleStatus, handleStatus);
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
        if (semester != null) wrapper.eq(RiskWarning::getSemester, semester);
        if (riskLevel != null) wrapper.eq(RiskWarning::getRiskLevel, riskLevel);
        if (handleStatus != null) wrapper.eq(RiskWarning::getHandleStatus, handleStatus);
        wrapper.orderByDesc(RiskWarning::getRiskLevel).orderByAsc(RiskWarning::getHandleStatus);
        return riskWarningMapper.selectList(wrapper).stream()
                .map(converter::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void handle(Long id, String remark, Long handlerId) {
        RiskWarning warning = riskWarningMapper.selectById(id);
        if (warning == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        warning.setHandleStatus("RESOLVED");
        warning.setHandlerId(handlerId);
        warning.setHandleRemark(remark);
        warning.setHandleAt(LocalDateTime.now());
        riskWarningMapper.updateById(warning);
    }

    private String buildRiskIndicators(List<Score> scores) {
        long failCount = scores.stream().filter(s -> s.getFinalScore() != null && (s.getFinalScore().compareTo(BigDecimal.valueOf(60)) < 0)).count();
        long absentCount = scores.stream().filter(s -> s.getIsAbsent() != null && s.getIsAbsent() == 1).count();
        long cheatCount = scores.stream().filter(s -> s.getIsCheat() != null && s.getIsCheat() == 1).count();

        return String.format("不及格科目数:%d 缺考次数:%d 作弊次数:%d", failCount, absentCount, cheatCount);
    }

    private String evaluateInitialRisk(List<Score> scores) {
        long failCount = scores.stream().filter(s -> s.getFinalScore() != null && (s.getFinalScore().compareTo(BigDecimal.valueOf(60)) < 0)).count();
        long absentCount = scores.stream().filter(s -> s.getIsAbsent() != null && s.getIsAbsent() == 1).count();

        if (failCount >= 3 || absentCount >= 2) return "HIGH";
        if (failCount >= 1 || absentCount >= 1) return "MEDIUM";
        return "NONE";
    }
}