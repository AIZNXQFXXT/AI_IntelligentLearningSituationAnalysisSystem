package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.ClassInfo;
import com.campus.common.vo.ClassExportVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ClassMapper extends BaseMapper<ClassInfo> {

    @Select("SELECT c.id, c.grade, c.class_name, c.student_count, " +
            "t.name AS head_teacher_name, " +
            "c.created_at, c.updated_at " +
            "FROM class_info c " +
            "LEFT JOIN teacher t ON c.head_teacher_id = t.id AND t.is_deleted = 0 " +
            "WHERE c.is_deleted = 0 ORDER BY c.id ASC")
    List<ClassExportVO> selectExportList();

    @Select("SELECT * FROM class_info WHERE class_name = #{className} AND grade = #{grade} AND is_deleted = 0")
    ClassInfo selectByClassNameAndGrade(@Param("className") String className, @Param("grade") String grade);

    @Select("SELECT * FROM class_info WHERE class_name = #{className} AND is_deleted = 0 LIMIT 1")
    ClassInfo selectByClassName(@Param("className") String className);
}