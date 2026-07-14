package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.Score;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

public interface ScoreMapper extends BaseMapper<Score> {

    @Select("SELECT AVG(final_score) FROM score WHERE exam_id = #{examId} AND course_id = #{courseId} AND is_deleted = 0")
    BigDecimal avgScoreByExamAndCourse(@Param("examId") Long examId, @Param("courseId") Long courseId);
}
