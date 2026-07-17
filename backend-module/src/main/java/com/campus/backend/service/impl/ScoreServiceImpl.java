package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.converter.ScoreConverter;
import com.campus.backend.entity.Course;
import com.campus.backend.entity.Score;
import com.campus.backend.entity.ScoreCorrection;
import com.campus.backend.entity.Student;
import com.campus.backend.mapper.CourseMapper;
import com.campus.backend.mapper.ScoreCorrectionMapper;
import com.campus.backend.mapper.ScoreMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.service.ScoreService;
import com.campus.common.dto.ScoreDTO;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import com.campus.common.vo.ScoreArchiveVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ScoreServiceImpl implements ScoreService {
    private final ScoreMapper scoreMapper;
    private final ScoreCorrectionMapper correctionMapper;
    private final ScoreConverter converter;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public Score create(ScoreDTO dto, Long enteredBy) {
        // 解析 studentNo → studentId
        if (dto.getStudentNo() != null) {
            Student s = studentMapper.selectByStudentNo(dto.getStudentNo());
            if (s == null) throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "学号不存在");
            dto.setStudentId(s.getId());
        }
        // 解析 courseName → courseId
        if (dto.getCourseName() != null) {
            Course c = courseMapper.selectByName(dto.getCourseName());
            if (c == null) throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "课程不存在");
            dto.setCourseId(c.getId());
        }
        // 校验重复成绩
        Long count = scoreMapper.selectCount(new LambdaQueryWrapper<Score>()
                .eq(Score::getStudentId, dto.getStudentId())
                .eq(Score::getExamId, dto.getExamId())
                .eq(Score::getCourseId, dto.getCourseId()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT);
        }
        Score entity = converter.toEntity(dto);
        entity.setEnteredBy(enteredBy);
        scoreMapper.insert(entity);

        // 更新排名（异步或同步计算）
        updateRankings(entity.getExamId(), entity.getCourseId());
        return entity;
    }

    @Override
    @Transactional
    public Score updateScore(Long id, ScoreDTO dto, Long operatorId) {
        if (dto.getFinalScore() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
        if (dto.getReason() == null || dto.getReason().isEmpty()) {
            throw new BusinessException(403, "修改成绩必须填写原因");
        }
        Score oldScore = scoreMapper.selectById(id);
        if (oldScore == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        ScoreCorrection correction = new ScoreCorrection();
        correction.setScoreId(id);
        correction.setOldFinalScore(oldScore.getFinalScore());
        correction.setNewFinalScore(dto.getFinalScore());
        correction.setReason(dto.getReason());
        correction.setOperatorId(operatorId);
        correction.setOperatedAt(LocalDateTime.now());
        correctionMapper.insert(correction);
        // 更新成绩
        oldScore.setFinalScore(dto.getFinalScore());
        oldScore.setReason(dto.getReason());
        scoreMapper.updateById(oldScore);
        return oldScore;
    }

    @Override
    public Score findById(Long id) {
        return scoreMapper.selectById(id);
    }

    @Override
    public IPage<Score> pageList(int page, int size, Long examId, Long courseId, BigDecimal minScore, BigDecimal maxScore, Long classId) {
        Page<Score> p = new Page<>(page, size);
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(examId != null, Score::getExamId, examId)
                .eq(courseId != null, Score::getCourseId, courseId)
                .ge(minScore != null, Score::getFinalScore, minScore)
                .le(maxScore != null, Score::getFinalScore, maxScore);
        wrapper.orderByAsc(Score::getId);
        return scoreMapper.selectPage(p, wrapper);
    }

    @Override
    public IPage<ScoreArchiveVO> archiveOverview(int page, int size) {
        Page<ScoreArchiveVO> p = new Page<>(page, size);
        return scoreMapper.selectArchivePage(p);
    }

    @Override
    public void updateStatus(Long id, String status) {
        LambdaUpdateWrapper<Score> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Score::getId, id)
                .eq(Score::getIsDeleted, 0);
        updateWrapper.set(Score::getAuditStatus, status);
        int rows = scoreMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "成绩记录不存在或已被删除");
        }
    }

    @Override
    public void batchImport(MultipartFile file, Long teacherId) {

    }

    @Override
    public List<ScoreArchiveVO> exportList(Long examId, Long courseId, Long classId) {
        return scoreMapper.selectExportList(examId, courseId, classId);
    }

    private void updateRankings(Long examId, Long courseId) {
        // 计算该考试该课程的所有学生排名
        List<Score> scores = scoreMapper.selectList(new LambdaQueryWrapper<Score>()
                .eq(Score::getExamId, examId)
                .eq(Score::getCourseId, courseId)
                .orderByDesc(Score::getFinalScore));
        int rank = 1;
        for (Score s : scores) {
            s.setRankClass(rank++);
            scoreMapper.updateById(s);
        }
    }
}