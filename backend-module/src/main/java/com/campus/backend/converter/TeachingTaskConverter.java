package com.campus.backend.converter;

import com.campus.backend.entity.TeachingTask;
import com.campus.common.dto.TeachingTaskDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TeachingTaskConverter {
    public TeachingTask toEntity(TeachingTaskDTO dto) {
        TeachingTask entity = new TeachingTask();
        entity.setId(dto.getId());
        entity.setTeacherId(dto.getTeacherId());
        entity.setClassId(dto.getClassId());
        entity.setCourseId(dto.getCourseId());
        entity.setSemester(dto.getSemester());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    public TeachingTaskDTO toDTO(TeachingTask entity) {
        TeachingTaskDTO dto = new TeachingTaskDTO();
        dto.setId(entity.getId());
        dto.setTeacherId(entity.getTeacherId());
        dto.setClassId(entity.getClassId());
        dto.setCourseId(entity.getCourseId());
        dto.setSemester(entity.getSemester());
        return dto;
    }
}
