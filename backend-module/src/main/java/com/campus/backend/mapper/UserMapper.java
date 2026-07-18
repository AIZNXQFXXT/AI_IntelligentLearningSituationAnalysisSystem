package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} LIMIT 1")
    User selectByUsernameIncludeDeleted(@Param("username") String username);

    @Update("UPDATE sys_user SET is_deleted = 0, updated_at = NOW() WHERE username = #{username}")
    int recoverByUsername(@Param("username") String username);

    @Update("UPDATE sys_user SET is_deleted = 0, updated_at = NOW() WHERE id = #{id}")
    int recoverById(@Param("id") Long id);
}