package com.campus.backend.converter;

import com.campus.backend.entity.AIComment;
import com.campus.backend.entity.ClassInfo;
import com.campus.backend.entity.Student;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.common.vo.CommentVO;
import org.springframework.stereotype.Component;

@Component
public class CommentConverter {

    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;

    public CommentConverter(StudentMapper studentMapper, ClassMapper classMapper) {
        this.studentMapper = studentMapper;
        this.classMapper = classMapper;
    }

    public CommentVO toVO(AIComment entity) {
        if (entity == null) return null;
        CommentVO vo = new CommentVO();
        vo.setId(entity.getId());
        vo.setStudentId(entity.getStudentId());
        vo.setSemester(entity.getSemester());
        vo.setContent(entity.getContent());
        vo.setIsTeacherEdited(entity.getIsTeacherEdited() != null && entity.getIsTeacherEdited() == 1);
        vo.setStatus(entity.getStatus());
        vo.setGeneratedBy(entity.getGeneratedBy());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());

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

        return vo;
    }
}