package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.Course;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface CourseMapper extends BaseMapper<Course> {
    @Select("SELECT * FROM course WHERE name = #{name} AND is_deleted = 0")
    Course selectByName(@Param("name") String name);

    @Select("SELECT * FROM course WHERE name = #{name} LIMIT 1")
    Course selectByNameIncludeDeleted(@Param("name") String name);

    @Update("UPDATE course SET is_deleted = 0, updated_at = NOW() WHERE name = #{name}")
    int recoverByName(@Param("name") String name);
}
