package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.Student;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StudentMapper extends BaseMapper<Student> {
    @Select("SELECT * FROM student WHERE class_id = #{classId} AND is_deleted = 0")
    List<Student> selectListByClassId(Long classId);

    @Select("SELECT * FROM student WHERE user_id = #{userId} AND is_deleted = 0")
    Student selectByUserId(@Param("userId") Long userId);
}