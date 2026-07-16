package com.campus.backend.controller;

import com.alibaba.excel.EasyExcel;
import com.campus.backend.service.CommentService;
import com.campus.backend.service.RiskWarningService;
import com.campus.backend.service.ScoreService;
import com.campus.backend.service.StatsService;
import com.campus.backend.util.SecurityHelper;
import com.campus.common.dto.report.CommentRow;
import com.campus.common.dto.report.RiskRow;
import com.campus.common.dto.report.ScoreTableRow;
import com.campus.common.vo.ClassStatsVO;
import com.campus.common.vo.CommentVO;
import com.campus.common.vo.RiskWarningVO;
import com.campus.common.vo.ScoreArchiveVO;
import com.campus.common.vo.ScoreDistributionVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports/excel")
public class ReportController {

    private final ScoreService scoreService;
    private final CommentService commentService;
    private final RiskWarningService riskWarningService;
    private final StatsService statsService;

    public ReportController(ScoreService scoreService, CommentService commentService,
                            RiskWarningService riskWarningService, StatsService statsService) {
        this.scoreService = scoreService;
        this.commentService = commentService;
        this.riskWarningService = riskWarningService;
        this.statsService = statsService;
    }

    private static void setDownloadHeaders(HttpServletResponse response, String filename) throws UnsupportedEncodingException {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
    }

    @GetMapping("/score-table")
    public void exportScoreTable(
            @RequestParam Long examId,
            @RequestParam Long courseId,
            @RequestParam(required = false) Long classId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        List<ScoreArchiveVO> list = scoreService.exportList(examId, courseId, classId);
        List<ScoreTableRow> rows = list.stream().map(s -> new ScoreTableRow(
                s.getStudentNo(), s.getStudentName(), s.getClassName(),
                s.getCourseName(), s.getExamName(),
                s.getRegularScore(), s.getExamScore(), s.getFinalScore(),
                s.getRankClass(),
                s.getIsAbsent() != null && s.getIsAbsent() == 1 ? "是" : "否",
                s.getIsCheat() != null && s.getIsCheat() == 1 ? "是" : "否",
                s.getAuditStatus(), s.getSemester()
        )).collect(Collectors.toList());
        String examName = list.isEmpty() ? "成绩" : defaultIfBlank(list.get(0).getExamName(), "成绩");
        String courseName = list.isEmpty() ? "" : defaultIfBlank(list.get(0).getCourseName(), "");
        setDownloadHeaders(response, "班级成绩表_" + examName + "_" + courseName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), ScoreTableRow.class).sheet("成绩表").doWrite(rows);
    }

    @GetMapping("/comments")
    public void exportComments(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String semester,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        List<CommentVO> list = commentService.exportList(classId, semester);
        List<CommentRow> rows = list.stream().map(c -> new CommentRow(
                c.getStudentNo(), c.getStudentName(), c.getClassName(),
                c.getSemester(), extractContent(c.getContent()),
                Boolean.TRUE.equals(c.getIsTeacherEdited()) ? "是" : "否",
                c.getStatus()
        )).collect(Collectors.toList());
        setDownloadHeaders(response, "评语汇总.xlsx");
        EasyExcel.write(response.getOutputStream(), CommentRow.class).sheet("评语").doWrite(rows);
    }

    @GetMapping("/risk-list")
    public void exportRiskList(
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String handleStatus,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        List<RiskWarningVO> list = riskWarningService.exportList(semester, riskLevel, handleStatus);
        List<RiskRow> rows = list.stream().map(r -> new RiskRow(
                r.getStudentNo(), r.getStudentName(), r.getClassName(),
                r.getSemester(), r.getRiskLevel(), r.getRiskReason(),
                r.getHandleStatus(), r.getHandlerName(), r.getHandleAt()
        )).collect(Collectors.toList());
        setDownloadHeaders(response, "高风险清单.xlsx");
        EasyExcel.write(response.getOutputStream(), RiskRow.class).sheet("风险清单").doWrite(rows);
    }

    @GetMapping("/stats")
    public void exportStats(
            @RequestParam Long examId,
            @RequestParam Long courseId,
            @RequestParam Long classId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        ClassStatsVO stats = statsService.getClassStats(classId, examId, courseId);
        List<ScoreDistributionVO> distribution = statsService.getScoreDistribution(examId, courseId, classId);

        List<List<String>> data = new ArrayList<>();
        data.add(List.of("指标", "数值"));
        if (stats != null) {
            data.add(List.of("班级", safeStr(stats.getClassName())));
            data.add(List.of("总人数", String.valueOf(stats.getTotalStudents())));
            data.add(List.of("参考人数", String.valueOf(stats.getScoredStudents())));
            data.add(List.of("平均分", String.valueOf(stats.getAvgScore())));
            data.add(List.of("最高分", String.valueOf(stats.getMaxScore())));
            data.add(List.of("最低分", String.valueOf(stats.getMinScore())));
            data.add(List.of("中位数", String.valueOf(stats.getMedianScore())));
            data.add(List.of("及格率", stats.getPassRate() + "%"));
            data.add(List.of("优秀(90+)", String.valueOf(stats.getExcellentCount())));
            data.add(List.of("良好(80-89)", String.valueOf(stats.getGoodCount())));
            data.add(List.of("中等(70-79)", String.valueOf(stats.getMediumCount())));
            data.add(List.of("及格(60-69)", String.valueOf(stats.getPassCount())));
            data.add(List.of("不及格(<60)", String.valueOf(stats.getFailCount())));
        }
        data.add(List.of("", ""));
        data.add(List.of("分数段", "占比"));
        for (ScoreDistributionVO d : distribution) {
            data.add(List.of(d.getRangeLabel(), d.getPercentage() + "%"));
        }

        setDownloadHeaders(response, "统计报表.xlsx");
        EasyExcel.write(response.getOutputStream()).sheet("统计").doWrite(data);
    }

    private static String safeStr(Object o) {
        return o == null ? "" : o.toString();
    }

    private static String defaultIfBlank(String s, String def) {
        return s == null || s.isBlank() ? def : s;
    }

    private static String extractContent(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String trimmed = raw.trim();
        if (trimmed.startsWith("{")) {
            try {
                var json = new ObjectMapper().readTree(trimmed);
                if (json.has("comment")) return json.get("comment").asText();
                if (json.has("content")) return json.get("content").asText();
                if (json.has("text")) return json.get("text").asText();
            } catch (Exception ignored) {}
        }
        return trimmed;
    }
}
