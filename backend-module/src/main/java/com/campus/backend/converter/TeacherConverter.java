package com.campus.backend.converter;

import com.campus.backend.entity.Teacher;
import com.campus.common.dto.TeacherDTO;
import org.springframework.stereotype.Component;

@Component
public class TeacherConverter {
    public Teacher toEntity(TeacherDTO dto) {
        Teacher entity = new Teacher();
        entity.setId(dto.getId());
        entity.setTeacherNo(dto.getTeacherNo());
        entity.setName(dto.getName());
        entity.setTitle(dto.getTitle());
        entity.setSubject(dto.getSubject());
        entity.setEducation(dto.getEducation());
        entity.setDepartment(dto.getDepartment());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        return entity;
    }

    public TeacherDTO toDTO(Teacher entity) {
        TeacherDTO dto = new TeacherDTO();
        dto.setId(entity.getId());
        dto.setTeacherNo(entity.getTeacherNo());
        dto.setName(entity.getName());
        dto.setTitle(entity.getTitle());
        dto.setSubject(entity.getSubject());
        dto.setEducation(entity.getEducation());
        dto.setDepartment(entity.getDepartment());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}