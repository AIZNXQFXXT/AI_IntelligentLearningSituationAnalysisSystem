package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.AICommentVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AICommentVersionMapper extends BaseMapper<AICommentVersion> {
    @Select("SELECT COALESCE(MAX(version_no), 0) FROM ai_comment_version WHERE comment_id = #{commentId} AND is_deleted = 0")
    int selectMaxVersion(Long commentId);
}