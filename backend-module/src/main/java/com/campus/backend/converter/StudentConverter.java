package com.campus.backend.converter;

import com.campus.backend.entity.Student;
import com.campus.common.dto.StudentDTO;
import org.springframework.stereotype.Component;

@Component
public class StudentConverter {
    public Student toEntity(StudentDTO dto) {
        Student entity = new Student();
        entity.setId(dto.getId());
        entity.setStudentNo(dto.getStudentNo());
        entity.setName(dto.getName());
        entity.setGender(dto.getGender());
        entity.setClassId(dto.getClassId());
        entity.setEnrollYear(dto.getEnrollYear());
        entity.setStatus(dto.getStatus());
        entity.setPhone(dto.getPhone());
        entity.setGuardianPhone(dto.getGuardianPhone());
        return entity;
    }

    public StudentDTO toDTO(Student entity) {
        StudentDTO dto = new StudentDTO();
        dto.setId(entity.getId());
        dto.setStudentNo(entity.getStudentNo());
        dto.setName(entity.getName());
        dto.setGender(entity.getGender());
        dto.setClassId(entity.getClassId());
        dto.setEnrollYear(entity.getEnrollYear());
        dto.setStatus(entity.getStatus());
        dto.setPhone(entity.getPhone());
        dto.setGuardianPhone(entity.getGuardianPhone());
        return dto;
    }
}
