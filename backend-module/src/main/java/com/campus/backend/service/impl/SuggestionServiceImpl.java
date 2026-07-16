package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.backend.ai.AiRequest;
import com.campus.backend.ai.AiServiceFactory;
import com.campus.backend.converter.SuggestionConverter;
import com.campus.backend.entity.AIDiagnosisRecord;
import com.campus.backend.entity.AISuggestion;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Student;
import com.campus.backend.mapper.AIDiagnosisRecordMapper;
import com.campus.backend.mapper.AISuggestionMapper;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.service.SuggestionService;
import com.campus.common.constant.PromptTemplate;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import com.campus.common.vo.SuggestionVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestionServiceImpl implements SuggestionService {

    private final AISuggestionMapper suggestionMapper;
    private final AIDiagnosisRecordMapper diagnosisMapper;
    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;
    private final AiServiceFactory aiServiceFactory;
    private final SuggestionConverter converter;

    @Override
    public SuggestionVO getSuggestion(Long studentId, String semester, Long callerId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        LambdaQueryWrapper<AIDiagnosisRecord> diagQuery = new LambdaQueryWrapper<AIDiagnosisRecord>()
                .eq(AIDiagnosisRecord::getStudentId, studentId)
                .eq(AIDiagnosisRecord::getSemester, semester)
                .orderByDesc(AIDiagnosisRecord::getCreatedAt)
                .last("LIMIT 1");
        List<AIDiagnosisRecord> diagList = diagnosisMapper.selectList(diagQuery);
        if (diagList.isEmpty()) {
            throw new BusinessException(500, "该学生暂无学情诊断，请先生成诊断");
        }
        AIDiagnosisRecord latestDiagnosis = diagList.get(0);

        LambdaQueryWrapper<AISuggestion> cacheQuery = new LambdaQueryWrapper<AISuggestion>()
                .eq(AISuggestion::getStudentId, studentId)
                .eq(AISuggestion::getSemester, semester)
                .eq(AISuggestion::getDiagnosisId, latestDiagnosis.getId())
                .orderByDesc(AISuggestion::getCreatedAt)
                .last("LIMIT 1");
        List<AISuggestion> cached = suggestionMapper.selectList(cacheQuery);
        if (!cached.isEmpty()) {
            return converter.toVO(cached.get(0));
        }

        String grade = "";
        if (student.getClassId() != null) {
            ClassInfo classInfo = classMapper.selectById(student.getClassId());
            if (classInfo != null && classInfo.getGrade() != null) {
                grade = classInfo.getGrade();
            }
        }

        String diagnosisSummary = latestDiagnosis.getDiagnosisText();
        try {
            var json = new ObjectMapper().readTree(diagnosisSummary);
            if (json.has("overall")) {
                diagnosisSummary = json.get("overall").asText();
            }
        } catch (Exception e) {
            // use raw text
        }

        String prompt = String.format(PromptTemplate.SUGGESTION_PROMPT,
                student.getName(), grade, diagnosisSummary);

        AiRequest request = AiRequest.builder()
                .prompt(prompt)
                .model("deepseek-chat")
                .callerId(callerId)
                .functionName("suggestion")
                .promptTemplate("SUGGESTION_PROMPT")
                .temperature(0.7)
                .maxTokens(2048)
                .build();

        var aiResult = aiServiceFactory.execute(request);

        if (!aiResult.isSuccess()) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR.getCode(),
                    "AI 学习建议生成失败：" + aiResult.getErrorMessage());
        }

        String cleanedContent = aiResult.getContent()
                .replaceAll("^```json\\s*", "")
                .replaceAll("```$", "")
                .trim();

        AISuggestion entity = new AISuggestion();
        entity.setStudentId(studentId);
        entity.setSemester(semester);
        entity.setDiagnosisId(latestDiagnosis.getId());
        entity.setContent(cleanedContent);
        entity.setTokensUsed(aiResult.getTokensTotal());

        try {
            var json = new ObjectMapper().readTree(cleanedContent);
            if (json.has("shortTerm")) entity.setShortTerm(json.get("shortTerm").toString());
            if (json.has("longTerm")) entity.setLongTerm(json.get("longTerm").toString());
            if (json.has("dailyPlan")) entity.setDailyPlan(json.get("dailyPlan").asText());
            if (json.has("resources")) entity.setResources(json.get("resources").toString());
        } catch (Exception e) {
            log.warn("Failed to parse suggestion JSON", e);
        }

        suggestionMapper.insert(entity);
        return converter.toVO(entity);
    }
}
