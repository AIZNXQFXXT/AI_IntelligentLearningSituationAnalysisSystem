package com.campus.backend.converter;

import com.campus.backend.entity.ClassInfo;
import com.campus.common.dto.ClassDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ClassConverter {
    public ClassInfo toEntity(ClassDTO dto) {
        ClassInfo entity = new ClassInfo();
        entity.setId(dto.getId());
        entity.setGrade(dto.getGrade());
        entity.setClassName(dto.getClassName());
        entity.setHeadTeacherId(dto.getHeadTeacherId());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    public ClassDTO toDTO(ClassInfo entity) {
        ClassDTO dto = new ClassDTO();
        dto.setId(entity.getId());
        dto.setGrade(entity.getGrade());
        dto.setClassName(entity.getClassName());
        dto.setHeadTeacherId(entity.getHeadTeacherId());
        return dto;
    }
}