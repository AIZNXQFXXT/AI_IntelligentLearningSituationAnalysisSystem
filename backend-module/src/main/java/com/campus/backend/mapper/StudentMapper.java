package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.Student;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface StudentMapper extends BaseMapper<Student> {
    @Select("SELECT * FROM student WHERE class_id = #{classId} AND is_deleted = 0")
    List<Student> selectListByClassId(Long classId);

    @Select("SELECT * FROM student WHERE user_id = #{userId} AND is_deleted = 0")
    Student selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM student WHERE student_no = #{studentNo} AND is_deleted = 0")
    Student selectByStudentNo(@Param("studentNo") String studentNo);

    @Select("SELECT * FROM student WHERE student_no = #{studentNo} LIMIT 1")
    Student selectByStudentNoIncludeDeleted(@Param("studentNo") String studentNo);

    @Update("UPDATE student SET is_deleted = 0, updated_at = NOW() WHERE student_no = #{studentNo}")
    int recoverByStudentNo(@Param("studentNo") String studentNo);
}