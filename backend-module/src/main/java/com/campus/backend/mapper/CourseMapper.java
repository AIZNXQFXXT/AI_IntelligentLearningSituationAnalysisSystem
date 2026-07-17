package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.Course;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CourseMapper extends BaseMapper<Course> {
    @Select("SELECT * FROM course WHERE name = #{name} AND is_deleted = 0")
    Course selectByName(@Param("name") String name);
}
