package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.Teacher;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface TeacherMapper extends BaseMapper<Teacher> {
    @Select("SELECT * FROM teacher WHERE teacher_no = #{teacherNo} AND is_deleted = 0")
    Teacher selectByTeacherNo(@Param("teacherNo") String teacherNo);
}