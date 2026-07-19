package com.campus.backend.async;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.backend.ai.AiRequest;
import com.campus.backend.ai.AiServiceFactory;
import com.campus.backend.entity.AIComment;
import com.campus.backend.entity.AICommentVersion;
import com.campus.backend.entity.Score;
import com.campus.backend.entity.Student;
import com.campus.backend.mapper.AICommentMapper;
import com.campus.backend.mapper.AICommentVersionMapper;
import com.campus.backend.mapper.ScoreMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.service.TaskService;
import com.campus.common.constant.PromptTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CommentBatchTask implements Runnable {

    private final Long taskId;
    private final Long classId;
    private final String semester;
    private final Long teacherId;
    private final AICommentMapper commentMapper;
    private final AICommentVersionMapper versionMapper;
    private final StudentMapper studentMapper;
    private final ScoreMapper scoreMapper;
    private final AiServiceFactory aiServiceFactory;
    private final TaskService taskService;

    @Override
    public void run() {
        try {
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getClassId, classId)
            );

            if (students.isEmpty()) {
                taskService.complete(taskId, "{\"success\":0,\"skipped\":0,\"failed\":0,\"errors\":[]}");
                return;
            }

            taskService.updateProgress(taskId, 10, 0, students.size());

            int success = 0, skipped = 0, failed = 0;
            List<Map<String, Object>> errors = new ArrayList<>();

            for (int i = 0; i < students.size(); i++) {
                Student student = students.get(i);
                try {
                    List<AIComment> existing = commentMapper.selectList(
                            new LambdaQueryWrapper<AIComment>()
                                    .eq(AIComment::getStudentId, student.getId())
                                    .eq(AIComment::getSemester, semester)
                                    .last("LIMIT 1")
                    );
                    if (!existing.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    List<Score> scores = scoreMapper.selectList(
                            new LambdaQueryWrapper<Score>()
                                    .eq(Score::getStudentId, student.getId())
                                    .orderByAsc(Score::getCreatedAt)
                    );

                    String scoresText = scores.stream()
                            .map(s -> String.format("课程:%d 成绩:%.1f 排名:%d",
                                    s.getCourseId(),
                                    s.getFinalScore() != null ? s.getFinalScore().doubleValue() : 0,
                                    s.getRankClass() != null ? s.getRankClass() : 0))
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
                        failed++;
                        Map<String, Object> error = new HashMap<>();
                        error.put("studentId", student.getId());
                        error.put("reason", aiResult.getErrorMessage());
                        errors.add(error);
                        continue;
                    }

                    String cleanedContent = com.campus.backend.ai.AiUtils.extractJsonContent(aiResult.getContent());

                    AIComment comment = new AIComment();
                    comment.setStudentId(student.getId());
                    comment.setTeacherId(teacherId);
                    comment.setSemester(semester);
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

                    success++;
                } catch (Throwable e) {
                    failed++;
                    Map<String, Object> error = new HashMap<>();
                    error.put("studentId", student.getId());
                    error.put("reason", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                    errors.add(error);
                }

                if (i % 10 == 0 || i == students.size() - 1) {
                    int progress = 10 + (int) ((long) (i + 1) * 80 / students.size());
                    taskService.updateProgress(taskId, progress, success + skipped + failed, students.size());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("skipped", skipped);
            result.put("failed", failed);
            result.put("errors", errors);
            taskService.complete(taskId, new ObjectMapper().writeValueAsString(result));

        } catch (Throwable e) {
            try {
                taskService.fail(taskId, e.getClass().getName() + ": " + (e.getMessage() != null ? e.getMessage() : ""));
            } catch (Throwable ignored) {
                // fail() also failed
            }
        }
    }
}
