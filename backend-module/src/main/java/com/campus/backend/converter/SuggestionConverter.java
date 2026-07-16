package com.campus.backend.converter;

import com.campus.backend.entity.AISuggestion;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Student;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.common.vo.SuggestionVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SuggestionConverter {

    private final ObjectMapper objectMapper;
    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;

    public SuggestionConverter(ObjectMapper objectMapper, StudentMapper studentMapper,
                                ClassMapper classMapper) {
        this.objectMapper = objectMapper;
        this.studentMapper = studentMapper;
        this.classMapper = classMapper;
    }

    public SuggestionVO toVO(AISuggestion entity) {
        if (entity == null) return null;
        SuggestionVO vo = new SuggestionVO();
        vo.setId(entity.getId());
        vo.setStudentId(entity.getStudentId());
        vo.setSemester(entity.getSemester());
        vo.setCreatedAt(entity.getCreatedAt());

        Student student = studentMapper.selectById(entity.getStudentId());
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
            JsonNode json = objectMapper.readTree(entity.getContent());
            if (json.has("shortTerm")) {
                List<String> list = new ArrayList<>();
                for (JsonNode item : json.get("shortTerm")) {
                    list.add(item.asText());
                }
                vo.setShortTerm(list);
            }
            if (json.has("longTerm")) {
                List<String> list = new ArrayList<>();
                for (JsonNode item : json.get("longTerm")) {
                    list.add(item.asText());
                }
                vo.setLongTerm(list);
            }
            if (json.has("dailyPlan")) {
                vo.setDailyPlan(json.get("dailyPlan").asText());
            }
            if (json.has("resources")) {
                List<SuggestionVO.ResourceItem> resources = new ArrayList<>();
                for (JsonNode item : json.get("resources")) {
                    SuggestionVO.ResourceItem ri = new SuggestionVO.ResourceItem();
                    if (item.has("subject")) ri.setSubject(item.get("subject").asText());
                    if (item.has("items")) {
                        List<String> items = new ArrayList<>();
                        for (JsonNode i : item.get("items")) {
                            items.add(i.asText());
                        }
                        ri.setItems(items);
                    }
                    resources.add(ri);
                }
                vo.setResources(resources);
            }
        } catch (Exception e) {
            // ignore parse errors
        }

        return vo;
    }
}
