package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.Teacher;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface TeacherMapper extends BaseMapper<Teacher> {
    @Select("SELECT * FROM teacher WHERE teacher_no = #{teacherNo} AND is_deleted = 0")
    Teacher selectByTeacherNo(@Param("teacherNo") String teacherNo);

    @Select("SELECT * FROM teacher WHERE teacher_no = #{teacherNo} LIMIT 1")
    Teacher selectByTeacherNoIncludeDeleted(@Param("teacherNo") String teacherNo);

    @Update("UPDATE teacher SET is_deleted = 0, updated_at = NOW() WHERE teacher_no = #{teacherNo}")
    int recoverByTeacherNo(@Param("teacherNo") String teacherNo);
}