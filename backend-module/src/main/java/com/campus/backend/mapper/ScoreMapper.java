package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.entity.Score;
import com.campus.common.vo.CourseGradeVO;
import com.campus.common.vo.ScoreArchiveVO;
import com.campus.common.vo.ScoreRadarVO;
import com.campus.common.vo.ScoreTrendVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

public interface ScoreMapper extends BaseMapper<Score> {

    @Select("SELECT AVG(final_score) FROM score WHERE exam_id = #{examId} AND course_id = #{courseId} AND is_deleted = 0")
    BigDecimal avgScoreByExamAndCourse(@Param("examId") Long examId, @Param("courseId") Long courseId);

    @Select("SELECT ROUND(AVG(final_score), 2) FROM score WHERE is_deleted = 0")
    Double avgFinalScore();

    @Select("SELECT COUNT(*) FROM score WHERE is_deleted = 0 AND final_score >= 60")
    long countPassed();

    @Select("SELECT COUNT(*) FROM score WHERE is_deleted = 0")
    long countTotal();

    @Select("SELECT COUNT(*) FROM score WHERE is_deleted = 0 AND final_score >= 90")
    long countExcellent();

    @Select("SELECT COUNT(*) FROM score WHERE is_deleted = 0 AND final_score >= 80 AND final_score < 90")
    long countGood();

    @Select("SELECT COUNT(*) FROM score WHERE is_deleted = 0 AND final_score >= 70 AND final_score < 80")
    long countMedium();

    @Select("SELECT COUNT(*) FROM score WHERE is_deleted = 0 AND final_score >= 60 AND final_score < 70")
    long countPass();

    @Select("SELECT COUNT(*) FROM score WHERE is_deleted = 0 AND final_score < 60")
    long countFail();

    @Select("SELECT s.id, s.student_id, s.exam_id, s.course_id, s.regular_score, s.exam_score, " +
            "s.final_score, s.rank_class, s.rank_grade, s.is_absent, s.is_cheat, " +
            "s.audit_status, s.entered_by, s.reason, s.created_at, s.updated_at, " +
            "stu.name AS student_name, stu.student_no, " +
            "c.name AS course_name, " +
            "e.name AS exam_name, " +
            "cl.id AS class_id, cl.class_name " +
            "FROM score s " +
            "LEFT JOIN student stu ON s.student_id = stu.id AND stu.is_deleted = 0 " +
            "LEFT JOIN course c ON s.course_id = c.id AND c.is_deleted = 0 " +
            "LEFT JOIN exam e ON s.exam_id = e.id AND e.is_deleted = 0 " +
            "LEFT JOIN class_info cl ON stu.class_id = cl.id AND cl.is_deleted = 0 " +
            "WHERE s.is_deleted = 0 " +
            "ORDER BY s.id DESC")
    IPage<ScoreArchiveVO> selectArchivePage(Page<?> page);

    @Select("<script>" +
            "SELECT s.id, s.student_id, s.exam_id, s.course_id, s.regular_score, s.exam_score, " +
            "s.final_score, s.rank_class, s.rank_grade, s.is_absent, s.is_cheat, " +
            "s.audit_status, s.created_at, s.updated_at, " +
            "stu.name AS student_name, stu.student_no, " +
            "c.name AS course_name, " +
            "e.name AS exam_name, e.semester, " +
            "cl.id AS class_id, cl.class_name " +
            "FROM score s " +
            "LEFT JOIN student stu ON s.student_id = stu.id AND stu.is_deleted = 0 " +
            "LEFT JOIN course c ON s.course_id = c.id AND c.is_deleted = 0 " +
            "LEFT JOIN exam e ON s.exam_id = e.id AND e.is_deleted = 0 " +
            "LEFT JOIN class_info cl ON stu.class_id = cl.id AND cl.is_deleted = 0 " +
            "WHERE s.is_deleted = 0 " +
            "AND s.exam_id = #{examId} " +
            "AND s.course_id = #{courseId} " +
            "<if test='classId != null'> AND stu.class_id = #{classId} </if>" +
            "ORDER BY s.id ASC" +
            "</script>")
    List<ScoreArchiveVO> selectExportList(@Param("examId") Long examId,
                                          @Param("courseId") Long courseId,
                                          @Param("classId") Long classId);

    @Select("<script>" +
            "SELECT s.id, s.student_id, s.exam_id, s.course_id, s.regular_score, s.exam_score, " +
            "s.final_score, s.rank_class, s.rank_grade, s.is_absent, s.is_cheat, " +
            "s.audit_status, s.created_at, s.updated_at, " +
            "stu.name AS student_name, stu.student_no, " +
            "c.name AS course_name, " +
            "e.name AS exam_name, e.semester, " +
            "cl.id AS class_id, cl.class_name " +
            "FROM score s " +
            "LEFT JOIN student stu ON s.student_id = stu.id AND stu.is_deleted = 0 " +
            "LEFT JOIN course c ON s.course_id = c.id AND c.is_deleted = 0 " +
            "LEFT JOIN exam e ON s.exam_id = e.id AND e.is_deleted = 0 " +
            "LEFT JOIN class_info cl ON stu.class_id = cl.id AND cl.is_deleted = 0 " +
            "WHERE s.is_deleted = 0 AND s.student_id = #{studentId} " +
            "<if test='semester != null and !semester.isEmpty()'> AND e.semester = #{semester} </if>" +
            "ORDER BY s.created_at DESC" +
            "</script>")
    IPage<ScoreArchiveVO> selectByStudentPage(Page<?> page,
                                              @Param("studentId") Long studentId,
                                              @Param("semester") String semester);

    @Select("SELECT e.semester, " +
            "ROUND(AVG(s.final_score), 2) AS avg_score, " +
            "MAX(s.final_score) AS max_score, " +
            "MIN(s.final_score) AS min_score, " +
            "COUNT(*) AS count, " +
            "ROUND(SUM(CASE " +
            "WHEN s.final_score >= 90 THEN (s.final_score - 90) * 0.1 + 4.0 " +
            "WHEN s.final_score >= 80 THEN (s.final_score - 80) * 0.1 + 3.0 " +
            "WHEN s.final_score >= 70 THEN (s.final_score - 70) * 0.1 + 2.0 " +
            "WHEN s.final_score >= 60 THEN (s.final_score - 60) * 0.1 + 1.0 " +
            "ELSE 0 END * COALESCE(c.credit, 1)) " +
            "/ NULLIF(SUM(COALESCE(c.credit, 1)), 0), 2) AS gpa " +
            "FROM score s " +
            "JOIN exam e ON s.exam_id = e.id AND e.is_deleted = 0 " +
            "LEFT JOIN course c ON s.course_id = c.id AND c.is_deleted = 0 " +
            "WHERE s.student_id = #{studentId} AND s.is_deleted = 0 AND s.final_score IS NOT NULL " +
            "GROUP BY e.semester ORDER BY e.semester")
    List<ScoreTrendVO> selectTrend(@Param("studentId") Long studentId);

    @Select("<script>" +
            "SELECT c.name AS course_name, s.final_score " +
            "FROM score s " +
            "JOIN course c ON s.course_id = c.id AND c.is_deleted = 0 " +
            "JOIN exam e ON s.exam_id = e.id AND e.is_deleted = 0 " +
            "WHERE s.student_id = #{studentId} AND s.is_deleted = 0 " +
            "<if test='semester != null and semester != \"\"'> AND e.semester = #{semester} </if>" +
            "ORDER BY c.name" +
            "</script>")
    List<ScoreRadarVO> selectRadar(@Param("studentId") Long studentId,
                                   @Param("semester") String semester);

    @Select("<script>" +
            "SELECT c.name AS course_name, " +
            "ROUND(AVG(s.final_score), 2) AS avg_score, " +
            "MAX(s.final_score) AS max_score, " +
            "MIN(s.final_score) AS min_score, " +
            "COUNT(s.final_score) AS student_count " +
            "FROM score s " +
            "JOIN student stu ON s.student_id = stu.id AND stu.is_deleted = 0 " +
            "JOIN course c ON s.course_id = c.id AND c.is_deleted = 0 " +
            "WHERE s.is_deleted = 0 " +
            "AND stu.class_id = #{classId} " +
            "AND s.final_score IS NOT NULL " +
            "GROUP BY c.id, c.name " +
            "ORDER BY c.name" +
            "</script>")
    List<CourseGradeVO> selectCourseGradeStats(@Param("classId") Long classId);
}
