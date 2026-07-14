package com.campus.backend.converter;

import com.campus.backend.entity.Exam;
import com.campus.common.dto.ExamDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ExamConverter {
    public Exam toEntity(ExamDTO dto) {
        Exam entity = new Exam();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setSemester(dto.getSemester());
        entity.setClassId(dto.getClassId());
        entity.setExamDate(dto.getExamDate());
        entity.setIsArchived(dto.getIsArchived());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    public ExamDTO toDTO(Exam entity) {
        ExamDTO dto = new ExamDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setSemester(entity.getSemester());
        dto.setClassId(entity.getClassId());
        dto.setExamDate(entity.getExamDate());
        dto.setIsArchived(entity.getIsArchived());
        return dto;
    }
}
