package com.campus.backend.converter;

import com.campus.backend.entity.Course;
import com.campus.common.dto.CourseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CourseConverter {
    public Course toEntity(CourseDTO dto) {
        Course entity = new Course();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setCredit(dto.getCredit());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    public CourseDTO toDTO(Course entity) {
        CourseDTO dto = new CourseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setCredit(entity.getCredit());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}
