package com.campus.backend.converter;

import com.campus.backend.entity.Score;
import com.campus.common.dto.ScoreDTO;
import org.springframework.stereotype.Component;

@Component
public class ScoreConverter {
    public Score toEntity(ScoreDTO dto) {
        Score entity = new Score();
        entity.setId(dto.getId());
        entity.setStudentId(dto.getStudentId());
        entity.setExamId(dto.getExamId());
        entity.setCourseId(dto.getCourseId());
        entity.setRegularScore(dto.getRegularScore());
        entity.setExamScore(dto.getExamScore());
        entity.setFinalScore(dto.getFinalScore());
        entity.setIsAbsent(dto.getIsAbsent() != null ? dto.getIsAbsent() : 0);
        entity.setIsCheat(dto.getIsCheat() != null ? dto.getIsCheat() : 0);
        entity.setAuditStatus("DRAFT");
        return entity;
    }
}
