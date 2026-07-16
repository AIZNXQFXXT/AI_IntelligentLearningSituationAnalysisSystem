package com.campus.backend.converter;

import com.campus.backend.entity.AIDiagnosisRecord;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Student;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.common.vo.DiagnosisVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DiagnosisConverter {

    private final ObjectMapper objectMapper;
    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;

    public DiagnosisConverter(ObjectMapper objectMapper, StudentMapper studentMapper, ClassMapper classMapper) {
        this.objectMapper = objectMapper;
        this.studentMapper = studentMapper;
        this.classMapper = classMapper;
    }

    public DiagnosisVO toVO(AIDiagnosisRecord record) {
        if (record == null) return null;
        DiagnosisVO vo = new DiagnosisVO();
        vo.setId(record.getId());
        vo.setStudentId(record.getStudentId());
        vo.setSemester(record.getSemester());
        vo.setDiagnosisText(record.getDiagnosisText());
        vo.setCreatedAt(record.getCreatedAt());
        vo.setRiskLevel(record.getRiskLevel());

        Student student = studentMapper.selectById(record.getStudentId());
        if (student != null) {
            vo.setStudentName(student.getName());
            if (student.getClassId() != null) {
                ClassInfo classInfo = classMapper.selectById(student.getClassId());
                if (classInfo != null) {
                    vo.setClassName(classInfo.getClassName());
                }
            }
        }

        try {
            JsonNode json = objectMapper.readTree(record.getDiagnosisText());
            if (json.has("overall")) vo.setOverall(json.get("overall").asText());
            if (json.has("trend")) vo.setTrend(json.get("trend").asText());
            if (json.has("riskLevel")) vo.setRiskLevel(json.get("riskLevel").asText());
            if (json.has("strengths")) {
                List<DiagnosisVO.StrengthItem> list = new ArrayList<>();
                for (JsonNode item : json.get("strengths")) {
                    DiagnosisVO.StrengthItem si = new DiagnosisVO.StrengthItem();
                    if (item.has("subject")) si.setSubject(item.get("subject").asText());
                    if (item.has("reason")) si.setReason(item.get("reason").asText());
                    list.add(si);
                }
                vo.setStrengths(list);
            }
            if (json.has("weaknesses")) {
                List<DiagnosisVO.WeaknessItem> list = new ArrayList<>();
                for (JsonNode item : json.get("weaknesses")) {
                    DiagnosisVO.WeaknessItem wi = new DiagnosisVO.WeaknessItem();
                    if (item.has("subject")) wi.setSubject(item.get("subject").asText());
                    if (item.has("reason")) wi.setReason(item.get("reason").asText());
                    list.add(wi);
                }
                vo.setWeaknesses(list);
            }
            if (json.has("suggestions")) {
                List<String> list = new ArrayList<>();
                for (JsonNode item : json.get("suggestions")) {
                    list.add(item.asText());
                }
                vo.setSuggestions(list);
            }
        } catch (Exception e) {
            // 解析失败则返回原始文本
        }
        return vo;
    }
}