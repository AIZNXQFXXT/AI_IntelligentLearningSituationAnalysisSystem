package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.ai.AiRequest;
import com.campus.backend.ai.AiServiceFactory;
import com.campus.backend.converter.CommentConverter;
import com.campus.backend.entity.AIComment;
import com.campus.backend.entity.AICommentVersion;
import com.campus.backend.entity.Score;
import com.campus.backend.entity.Student;
import com.campus.backend.mapper.AICommentMapper;
import com.campus.backend.mapper.AICommentVersionMapper;
import com.campus.backend.mapper.ScoreMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.service.CommentService;
import com.campus.common.constant.PromptTemplate;
import com.campus.common.dto.AICommentDTO;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import com.campus.common.vo.CommentVO;
import com.campus.common.vo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final AICommentMapper commentMapper;
    private final AICommentVersionMapper versionMapper;
    private final ScoreMapper scoreMapper;
    private final StudentMapper studentMapper;
    private final AiServiceFactory aiServiceFactory;
    private final CommentConverter converter;

    @Override
    public CommentVO generateSingle(AICommentDTO dto, Long teacherId) {
        if (dto.getStudentNo() != null) {
            Student s = studentMapper.selectByStudentNo(dto.getStudentNo());
            if (s == null) throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "学号不存在");
            dto.setStudentId(s.getId());
        }
        Student student = studentMapper.selectById(dto.getStudentId());
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        List<Score> scores = scoreMapper.selectList(
                new LambdaQueryWrapper<Score>()
                        .eq(Score::getStudentId, dto.getStudentId())
                        .orderByAsc(Score::getCreatedAt)
        );

        String scoresText = scores.stream()
                .map(s -> String.format("课程:%d 成绩:%.1f 排名:%d",
                        s.getCourseId(), s.getFinalScore().doubleValue(), s.getRankClass()))
                .collect(Collectors.joining("\n"));

        String prompt = String.format(PromptTemplate.COMMENT_PROMPT,
                student.getName(), "", scoresText);

        AiRequest request = AiRequest.builder()
                .prompt(prompt)
                .model(aiServiceFactory.getActiveModel())
                .callerId(teacherId)
                .functionName("comment")
                .promptTemplate("COMMENT_PROMPT")
                .temperature(0.8)
                .maxTokens(1024)
                .build();

        var aiResult = aiServiceFactory.execute(request);

        if (!aiResult.isSuccess()) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR.getCode(),
                    "AI 评语生成失败：" + aiResult.getErrorMessage());
        }

        String cleanedContent = com.campus.backend.ai.AiUtils.extractJsonContent(aiResult.getContent());

        AIComment comment = new AIComment();
        comment.setStudentId(dto.getStudentId());
        comment.setTeacherId(teacherId);
        comment.setSemester(dto.getSemester());
        comment.setContent(cleanedContent);
        comment.setGeneratedBy("AI");
        comment.setStatus("PENDING");
        comment.setTokensUsed(aiResult.getTokensTotal());
        commentMapper.insert(comment);

        AICommentVersion version = new AICommentVersion();
        version.setCommentId(comment.getId());
        version.setVersionNo(1);
        version.setContent(cleanedContent);
        version.setSource("AI_GENERATED");
        version.setTokensUsed(aiResult.getTokensTotal());
        versionMapper.insert(version);

        return converter.toVO(comment);
    }

    @Override
    @Transactional
    public CommentVO update(Long id, String content, Long teacherId) {
        AIComment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        int maxVersion = versionMapper.selectMaxVersion(comment.getId());

        comment.setContent(content);
        comment.setIsTeacherEdited(1);
        comment.setGeneratedBy("MANUAL");
        commentMapper.updateById(comment);

        AICommentVersion version = new AICommentVersion();
        version.setCommentId(comment.getId());
        version.setVersionNo(maxVersion + 1);
        version.setContent(content);
        version.setSource("TEACHER_EDITED");
        version.setTokensUsed(0);
        versionMapper.insert(version);

        return converter.toVO(comment);
    }

    @Override
    public PageResult<CommentVO> pageList(int page, int size, Long classId, String semester, String keyword) {
        LambdaQueryWrapper<AIComment> wrapper = new LambdaQueryWrapper<>();
        if (classId != null || (keyword != null && !keyword.isEmpty())) {
            LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<Student>().select(Student::getId);
            if (classId != null) {
                studentWrapper.eq(Student::getClassId, classId);
            }
            if (keyword != null && !keyword.isEmpty()) {
                studentWrapper.and(w -> w.like(Student::getName, keyword)
                        .or().like(Student::getStudentNo, keyword));
            }
            List<Long> studentIds = studentMapper.selectList(studentWrapper).stream()
                    .map(Student::getId).collect(Collectors.toList());
            if (studentIds.isEmpty()) {
                return PageResult.of(List.of(), 0, page, size);
            }
            wrapper.in(AIComment::getStudentId, studentIds);
        }
        if (semester != null) {
            wrapper.eq(AIComment::getSemester, semester);
        }
        wrapper.orderByDesc(AIComment::getCreatedAt);
        Page<AIComment> result = commentMapper.selectPage(new Page<>(page, size), wrapper);
        List<CommentVO> records = result.getRecords().stream()
                .map(converter::toVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), page, size);
    }

    @Override
    public List<CommentVO> exportList(Long classId, String semester) {
        LambdaQueryWrapper<AIComment> wrapper = new LambdaQueryWrapper<>();
        if (classId != null) {
            List<Long> studentIds = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getClassId, classId)
                            .select(Student::getId)
            ).stream().map(Student::getId).collect(Collectors.toList());
            if (studentIds.isEmpty()) return List.of();
            wrapper.in(AIComment::getStudentId, studentIds);
        }
        if (semester != null) {
            wrapper.eq(AIComment::getSemester, semester);
        }
        wrapper.orderByDesc(AIComment::getCreatedAt);
        return commentMapper.selectList(wrapper).stream()
                .map(converter::toVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<CommentVO> listByStudent(int page, int size, Long studentId, String semester) {
        LambdaQueryWrapper<AIComment> wrapper = new LambdaQueryWrapper<AIComment>()
                .eq(AIComment::getStudentId, studentId);
        if (semester != null && !semester.isEmpty()) {
            wrapper.eq(AIComment::getSemester, semester);
        }
        wrapper.orderByDesc(AIComment::getCreatedAt);
        Page<AIComment> result = commentMapper.selectPage(new Page<>(page, size), wrapper);
        List<CommentVO> records = result.getRecords().stream()
                .map(converter::toVO).collect(Collectors.toList());
        return PageResult.of(records, result.getTotal(), page, size);
    }
}