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
        vo.setRiskReason(entity.getRiskReason());
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
}