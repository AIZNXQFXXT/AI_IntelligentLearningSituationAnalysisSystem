package com.campus.backend.converter;

import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.RiskWarning;
import com.campus.backend.entity.Student;
import com.campus.backend.entity.User;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.mapper.UserMapper;
import com.campus.common.vo.RiskWarningVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RiskWarningConverter {

    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public RiskWarningConverter(StudentMapper studentMapper, ClassMapper classMapper,
                                UserMapper userMapper, ObjectMapper objectMapper) {
        this.studentMapper = studentMapper;
        this.classMapper = classMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    public RiskWarningVO toVO(RiskWarning entity) {
        if (entity == null) return null;
        RiskWarningVO vo = new RiskWarningVO();
        vo.setId(entity.getId());
        vo.setStudentId(entity.getStudentId());
        vo.setSemester(entity.getSemester());
        vo.setRiskLevel(entity.getRiskLevel());
        vo.setRiskReason(parseRiskReason(entity.getRiskReason()));
        vo.setAiAnalysis(entity.getAiAnalysis());
        vo.setHandleStatus(entity.getHandleStatus());
        vo.setHandleRemark(entity.getHandleRemark());
        vo.setHandleAt(entity.getHandleAt());
        vo.setCreatedAt(entity.getCreatedAt());

        Student student = studentMapper.selectById(entity.getStudentId());
        if (student != null) {
            vo.setStudentName(student.getName());
            vo.setStudentNo(student.getStudentNo());
            if (student.getClassId() != null) {
                ClassInfo classInfo = classMapper.selectById(student.getClassId());
                if (classInfo != null) {
                    vo.setClassName(classInfo.getClassName());
                }
            }
        }

        if (entity.getHandlerId() != null) {
            User user = userMapper.selectById(entity.getHandlerId());
            if (user != null) {
                vo.setHandlerName(user.getUsername());
            }
        }

        try {
            var json = objectMapper.readTree(entity.getAiAnalysis());
            if (json.has("recommendations")) {
                List<String> list = new ArrayList<>();
                for (var item : json.get("recommendations")) {
                    list.add(item.asText());
                }
                vo.setRecommendations(list);
            }
        } catch (Exception e) {
            // ignore parse errors
        }

        return vo;
    }

    /**
     * 解析 riskReason 字段。可能格式：
     * - JSON 数组 [{"subject":"数学","reason":"基础薄弱"}] → "数学：基础薄弱；英语：词汇不足"
     * - JSON 数组 ["基础薄弱","词汇不足"] → "基础薄弱；词汇不足"
     * - 普通文本 → 直接返回
     */
    private String parseRiskReason(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String trimmed = raw.trim();
        if (!trimmed.startsWith("[")) {
            return trimmed;
        }
        try {
            var node = objectMapper.readTree(trimmed);
            if (!node.isArray() || node.isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            for (var item : node) {
                if (sb.length() > 0) sb.append("；");
                if (item.isObject()) {
                    String subject = item.has("subject") ? item.get("subject").asText() : "";
                    String reason = item.has("reason") ? item.get("reason").asText() : "";
                    if (!subject.isEmpty() && !reason.isEmpty()) {
                        sb.append(subject).append("：").append(reason);
                    } else if (!reason.isEmpty()) {
                        sb.append(reason);
                    } else if (!subject.isEmpty()) {
                        sb.append(subject);
                    }
                } else {
                    String text = item.asText();
                    if (!text.isEmpty()) sb.append(text);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return trimmed;
        }
    }
}