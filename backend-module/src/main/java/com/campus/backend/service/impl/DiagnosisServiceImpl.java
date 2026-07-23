package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.ai.AiRequest;
import com.campus.backend.ai.AiResult;
import com.campus.backend.ai.AiServiceFactory;
import com.campus.backend.ai.AiUtils;
import com.campus.backend.converter.DiagnosisConverter;
import com.campus.backend.entity.AIDiagnosisRecord;
import com.campus.backend.entity.AISuggestion;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.RiskWarning;
import com.campus.backend.entity.Score;
import com.campus.backend.entity.Student;
import com.campus.backend.mapper.AIDiagnosisRecordMapper;
import com.campus.backend.mapper.AISuggestionMapper;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.RiskWarningMapper;
import com.campus.backend.mapper.ScoreMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.service.DiagnosisService;
import com.campus.common.constant.PromptTemplate;
import com.campus.common.dto.AIDiagnosisDTO;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import com.campus.common.vo.DiagnosisVO;
import com.campus.common.vo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiagnosisServiceImpl implements DiagnosisService {

    private final AIDiagnosisRecordMapper diagnosisMapper;
    private final AISuggestionMapper suggestionMapper;
    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;
    private final RiskWarningMapper riskWarningMapper;
    private final AiServiceFactory aiServiceFactory;
    private final DiagnosisConverter converter;

    @Override
    public DiagnosisVO diagnose(AIDiagnosisDTO dto, Long teacherId) {
        if (dto.getStudentNo() != null) {
            Student s = studentMapper.selectByStudentNo(dto.getStudentNo());
            if (s == null) throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "学号不存在");
            dto.setStudentId(s.getId());
        }
        Student student = studentMapper.selectById(dto.getStudentId());
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        List<Score> scores = scoreMapper.selectList(
                new LambdaQueryWrapper<Score>()
                        .eq(Score::getStudentId, dto.getStudentId())
                        .orderByAsc(Score::getCreatedAt)
        );

        if (scores.isEmpty()) {
            throw new BusinessException(500, "该学生暂无成绩数据，无法生成诊断");
        }

        String grade = "";
        String className = "";
        if (student.getClassId() != null) {
            ClassInfo classInfo = classMapper.selectById(student.getClassId());
            if (classInfo != null) {
                grade = classInfo.getGrade() != null ? classInfo.getGrade() : "";
                className = classInfo.getClassName() != null ? classInfo.getClassName() : "";
            }
        }

        String scoresText = formatScoresForPrompt(scores);
        String prompt = String.format(
                PromptTemplate.DIAGNOSIS_PROMPT,
                student.getName(), grade, className, scoresText
        );

        String activeModel = aiServiceFactory.getActiveModel();

        AiRequest request = AiRequest.builder()
                .prompt(prompt)
                .model(activeModel)
                .callerId(teacherId)
                .functionName("diagnosis")
                .promptTemplate("DIAGNOSIS_PROMPT")
                .temperature(0.7)
                .maxTokens(2048)
                .build();

        AiResult aiResult = aiServiceFactory.execute(request);

        if (!aiResult.isSuccess()) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR.getCode(),
                    "AI 诊断生成失败：" + aiResult.getErrorMessage());
        }

        String cleanedContent = com.campus.backend.ai.AiUtils.extractJsonContent(aiResult.getContent());

        AIDiagnosisRecord record = new AIDiagnosisRecord();
        record.setStudentId(dto.getStudentId());
        record.setSemester(dto.getSemester());
        record.setDiagnosisText(cleanedContent);
        record.setTokensUsed(aiResult.getTokensTotal());
        record.setCost(java.math.BigDecimal.valueOf(aiResult.getEstimatedCost()));
        record.setDurationMs((int) aiResult.getDurationMs());
        record.setAiModel(activeModel);
        record.setPromptTemplate("DIAGNOSIS_PROMPT");

        com.fasterxml.jackson.databind.JsonNode json;
        try {
            json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(cleanedContent);
            if (json.has("strengths")) record.setStrengths(json.get("strengths").toString());
            if (json.has("weaknesses")) record.setWeaknesses(json.get("weaknesses").toString());
            if (json.has("trend")) record.setTrendAnalysis(json.get("trend").asText());
            if (json.has("riskLevel")) record.setRiskLevel(json.get("riskLevel").asText());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("AI 诊断返回非法 JSON，原始内容：{}", cleanedContent, e);
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR.getCode(),
                    "AI 诊断返回格式异常，请重试或联系管理员");
        }

        diagnosisMapper.insert(record);

        if (json != null && json.has("suggestions") && json.get("suggestions").isArray()) {
            AISuggestion suggestion = new AISuggestion();
            suggestion.setStudentId(record.getStudentId());
            suggestion.setSemester(record.getSemester());
            suggestion.setDiagnosisId(record.getId());
            suggestion.setContent(json.get("suggestions").toString());
            suggestion.setTokensUsed(aiResult.getTokensTotal());
            suggestionMapper.insert(suggestion);
        }

        // 同步风险预警：从诊断结果提取 riskLevel 归一化后写入 risk_warning 表（同学期去重）
        syncRiskWarning(record, json);

        return converter.toVO(record);
    }

    @Override
    public PageResult<DiagnosisVO> pageHistory(int page, int size, Long studentId) {
        LambdaQueryWrapper<AIDiagnosisRecord> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(AIDiagnosisRecord::getStudentId, studentId);
        }
        wrapper.orderByDesc(AIDiagnosisRecord::getCreatedAt);
        Page<AIDiagnosisRecord> result = diagnosisMapper.selectPage(new Page<>(page, size), wrapper);
        List<DiagnosisVO> records = result.getRecords().stream()
                .map(converter::toVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), page, size);
    }

    @Override
    public int backfillRiskWarnings() {
        List<AIDiagnosisRecord> all = diagnosisMapper.selectList(
                new LambdaQueryWrapper<AIDiagnosisRecord>()
                        .orderByAsc(AIDiagnosisRecord::getCreatedAt));
        int count = 0;
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        for (AIDiagnosisRecord record : all) {
            try {
                var json = mapper.readTree(record.getDiagnosisText());
                syncRiskWarning(record, json);
                count++;
            } catch (Exception e) {
                log.warn("回填跳过诊断记录 {}，JSON 解析失败", record.getId(), e);
            }
        }
        return count;
    }

    private String formatScoresForPrompt(List<Score> scores) {
        StringBuilder sb = new StringBuilder();
        for (Score s : scores) {
            sb.append(String.format("考试ID:%d 课程ID:%d 平时分:%.1f 卷面分:%.1f 最终分:%.1f 班级排名:%d\n",
                    s.getExamId(), s.getCourseId(),
                    s.getRegularScore() != null ? s.getRegularScore().doubleValue() : 0,
                    s.getExamScore() != null ? s.getExamScore().doubleValue() : 0.0,
                    s.getFinalScore() != null ? s.getFinalScore().doubleValue() : 0,
                    s.getRankClass() != null ? s.getRankClass() : 0));
        }
        return sb.toString();
    }

    /**
     * 从诊断结果同步风险预警到 risk_warning 表。
     * 同一学生同一学期已有记录则更新风险字段，否则新增。
     * riskLevel 为 NONE/无/无法识别时跳过（不写入无风险记录）。
     */
    private void syncRiskWarning(AIDiagnosisRecord record, com.fasterxml.jackson.databind.JsonNode json) {
        if (json == null) return;
        String rawLevel = json.has("riskLevel") ? json.get("riskLevel").asText() : null;
        String normalized = AiUtils.normalizeRiskLevel(rawLevel);
        if (normalized == null) return;

        // 从诊断 JSON 提取风险原因描述
        String riskReason = "";
        if (json.has("weaknesses")) {
            riskReason = json.get("weaknesses").toString();
        } else if (json.has("trend")) {
            riskReason = json.get("trend").asText();
        }

        // 同学期去重：查是否已有风险预警记录
        RiskWarning existing = riskWarningMapper.selectOne(
                new LambdaQueryWrapper<RiskWarning>()
                        .eq(RiskWarning::getStudentId, record.getStudentId())
                        .eq(RiskWarning::getSemester, record.getSemester())
                        .eq(RiskWarning::getIsDeleted, 0)
                        .last("LIMIT 1"));

        if (existing != null) {
            existing.setRiskLevel(normalized);
            existing.setRiskReason(riskReason);
            existing.setAiAnalysis(record.getDiagnosisText());
            riskWarningMapper.updateById(existing);
        } else {
            RiskWarning warning = new RiskWarning();
            warning.setStudentId(record.getStudentId());
            warning.setSemester(record.getSemester());
            warning.setRiskLevel(normalized);
            warning.setRiskReason(riskReason);
            warning.setAiAnalysis(record.getDiagnosisText());
            warning.setHandleStatus("PENDING");
            riskWarningMapper.insert(warning);
        }
    }
}