package com.campus.client.controller;

import com.campus.client.session.UserSession;
import com.campus.client.util.ViewLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class MainLayoutController {

    private static MainLayoutController instance;

    public static MainLayoutController getInstance() {
        return instance;
    }

    @FXML private VBox menuContainer;
    @FXML private BorderPane contentArea;
    @FXML private Label pageTitle;
    @FXML private Label usernameLabel;

    private final List<Button> menuButtons = new ArrayList<>();
    private String currentRoute = null;

    private static class MenuItem {
        private final String path;
        private final String title;
        MenuItem(String path, String title) { this.path = path; this.title = title; }
        String path() { return path; }
        String title() { return title; }
    }

    private final MenuItem[] adminMenus = {
        new MenuItem("/admin/dashboard", "控制台"),
        new MenuItem("/admin/classes", "班级管理"),
        new MenuItem("/admin/teachers", "教师管理"),
        new MenuItem("/admin/students", "学生管理"),
        new MenuItem("/admin/courses", "课程管理"),
        new MenuItem("/admin/exams", "考试批次"),
        new MenuItem("/admin/teaching-tasks", "教学任务"),
        new MenuItem("/admin/stats", "全校学情"),
        new MenuItem("/admin/grade-analytics", "年级学情"),
        new MenuItem("/admin/subject-analytics", "学科分析"),
        new MenuItem("/admin/risk-distribution", "风险分布"),
        new MenuItem("/admin/config", "系统配置"),
        new MenuItem("/admin/logs", "操作日志"),
        new MenuItem("/admin/ai-logs", "AI调用日志")
    };

    private final MenuItem[] teacherMenus = {
        new MenuItem("/teacher/dashboard", "控制台"),
        new MenuItem("/teacher/students", "本班学生"),
        new MenuItem("/teacher/score/entry", "成绩录入"),
        new MenuItem("/teacher/score/import", "批量导入"),
        new MenuItem("/teacher/stats", "班级统计"),
        new MenuItem("/teacher/diagnosis", "AI诊断"),
        new MenuItem("/teacher/comment", "评语管理"),
        new MenuItem("/teacher/risk", "风险预警"),
        new MenuItem("/teacher/report", "报表导出"),
        new MenuItem("/teacher/profile", "个人中心")
    };

    private final MenuItem[] studentMenus = {
        new MenuItem("/student/dashboard", "控制台"),
        new MenuItem("/student/profile", "个人主页"),
        new MenuItem("/student/scores", "成绩查询"),
        new MenuItem("/student/analysis", "成绩分析"),
        new MenuItem("/student/diagnosis", "诊断报告"),

        new MenuItem("/student/comment", "期末评语"),
        new MenuItem("/student/risk", "我的预警")
    };

    private static final String ADMIN_DASHBOARD = "/admin/dashboard";
    private static final String ADMIN_CLASSES = "/admin/classes";
    private static final String ADMIN_TEACHERS = "/admin/teachers";
    private static final String ADMIN_STUDENTS = "/admin/students";
    private static final String ADMIN_COURSES = "/admin/courses";
    private static final String ADMIN_EXAMS = "/admin/exams";
    private static final String ADMIN_TEACHING_TASKS = "/admin/teaching-tasks";
    private static final String ADMIN_STATS = "/admin/stats";
    private static final String ADMIN_GRADE_ANALYTICS = "/admin/grade-analytics";
    private static final String ADMIN_SUBJECT_ANALYTICS = "/admin/subject-analytics";
    private static final String ADMIN_RISK_DISTRIBUTION = "/admin/risk-distribution";
    private static final String ADMIN_CONFIG = "/admin/config";
    private static final String ADMIN_LOGS = "/admin/logs";
    private static final String ADMIN_AI_LOGS = "/admin/ai-logs";
    private static final String TEACHER_DASHBOARD = "/teacher/dashboard";
    private static final String TEACHER_STUDENTS = "/teacher/students";
    private static final String SCORE_ENTRY = "/teacher/score/entry";
    private static final String SCORE_IMPORT = "/teacher/score/import";
    private static final String CLASS_STATS = "/teacher/stats";
    private static final String TEACHER_DIAGNOSIS = "/teacher/diagnosis";
    private static final String TEACHER_COMMENT = "/teacher/comment";
    private static final String TEACHER_RISK = "/teacher/risk";
    private static final String REPORT_EXPORT = "/teacher/report";
    private static final String TEACHER_PROFILE = "/teacher/profile";
    private static final String STUDENT_DASHBOARD = "/student/dashboard";
    private static final String STUDENT_PROFILE = "/student/profile";
    private static final String STUDENT_SCORES = "/student/scores";
    private static final String STUDENT_ANALYSIS = "/student/analysis";
    private static final String STUDENT_DIAGNOSIS = "/student/diagnosis";

    private static final String STUDENT_COMMENT = "/student/comment";
    private static final String STUDENT_RISK = "/student/risk";

    @FXML
    public void initialize() {
        instance = this;
        UserSession session = UserSession.getInstance();
        usernameLabel.setText(session.getUsername());

        MenuItem[] menus = getMenusForRole(session.getRole());
        buildSidebar(menus);

        navigateTo(menus[0].path());
    }

    private MenuItem[] getMenusForRole(String role) {
        switch (role) {
            case "ADMIN": return adminMenus;
            case "TEACHER": return teacherMenus;
            case "STUDENT": return studentMenus;
            default: return adminMenus;
        }
    }

    private void buildSidebar(MenuItem[] menus) {
        menuContainer.getChildren().clear();
        menuButtons.clear();

        for (MenuItem item : menus) {
            Button btn = new Button(item.title());
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.getStyleClass().add("sidebar-menu-item");
            btn.setOnAction(e -> navigateTo(item.path()));
            menuButtons.add(btn);
            menuContainer.getChildren().add(btn);
        }
    }

    public void navigateTo(String route) {
        if (route.equals(currentRoute)) return;
        currentRoute = route;

        UserSession session = UserSession.getInstance();
        MenuItem[] menus = getMenusForRole(session.getRole());
        for (int i = 0; i < menus.length; i++) {
            Button btn = menuButtons.get(i);
            btn.getStyleClass().remove("active");
            if (menus[i].path().equals(route)) {
                btn.getStyleClass().add("active");
            }
        }

        for (MenuItem item : menus) {
            if (item.path().equals(route)) {
                pageTitle.setText(item.title());
                break;
            }
        }

        switch (route) {
            case ADMIN_DASHBOARD: ViewLoader.loadFXMLInto(contentArea, "/fxml/AdminDashboardView.fxml"); break;
            case ADMIN_CLASSES: ViewLoader.loadFXMLInto(contentArea, "/fxml/ClassManagementView.fxml"); break;
            case ADMIN_TEACHERS: ViewLoader.loadFXMLInto(contentArea, "/fxml/TeacherManagementView.fxml"); break;
            case ADMIN_STUDENTS: ViewLoader.loadFXMLInto(contentArea, "/fxml/StudentManagementView.fxml"); break;
            case ADMIN_COURSES: ViewLoader.loadFXMLInto(contentArea, "/fxml/CourseManagementView.fxml"); break;
            case ADMIN_EXAMS: ViewLoader.loadFXMLInto(contentArea, "/fxml/ExamManagementView.fxml"); break;
            case ADMIN_TEACHING_TASKS: ViewLoader.loadFXMLInto(contentArea, "/fxml/TeachingTaskManagementView.fxml"); break;
            case ADMIN_STATS: ViewLoader.loadFXMLInto(contentArea, "/fxml/SchoolStatsView.fxml"); break;
            case ADMIN_GRADE_ANALYTICS: ViewLoader.loadFXMLInto(contentArea, "/fxml/GradeAnalyticsView.fxml"); break;
            case ADMIN_SUBJECT_ANALYTICS: ViewLoader.loadFXMLInto(contentArea, "/fxml/SubjectAnalyticsView.fxml"); break;
            case ADMIN_RISK_DISTRIBUTION: ViewLoader.loadFXMLInto(contentArea, "/fxml/RiskDistributionView.fxml"); break;
            case ADMIN_CONFIG: ViewLoader.loadFXMLInto(contentArea, "/fxml/ConfigView.fxml"); break;
            case ADMIN_LOGS: ViewLoader.loadFXMLInto(contentArea, "/fxml/OperationLogView.fxml"); break;
            case ADMIN_AI_LOGS: ViewLoader.loadFXMLInto(contentArea, "/fxml/AiCallLogView.fxml"); break;
            case TEACHER_DASHBOARD: ViewLoader.loadFXMLInto(contentArea, "/fxml/TeacherDashboardView.fxml"); break;
            case TEACHER_STUDENTS: ViewLoader.loadFXMLInto(contentArea, "/fxml/TeacherStudentsView.fxml"); break;
            case SCORE_ENTRY: ViewLoader.loadFXMLInto(contentArea, "/fxml/ScoreEntryView.fxml"); break;
            case SCORE_IMPORT: ViewLoader.loadFXMLInto(contentArea, "/fxml/ScoreImportView.fxml"); break;
            case CLASS_STATS: ViewLoader.loadFXMLInto(contentArea, "/fxml/ClassStatsView.fxml"); break;
            case TEACHER_DIAGNOSIS: ViewLoader.loadFXMLInto(contentArea, "/fxml/TeacherDiagnosisView.fxml"); break;
            case TEACHER_COMMENT: ViewLoader.loadFXMLInto(contentArea, "/fxml/TeacherCommentView.fxml"); break;
            case TEACHER_RISK: ViewLoader.loadFXMLInto(contentArea, "/fxml/TeacherRiskView.fxml"); break;
            case REPORT_EXPORT: ViewLoader.loadFXMLInto(contentArea, "/fxml/ReportExportView.fxml"); break;
            case TEACHER_PROFILE: ViewLoader.loadFXMLInto(contentArea, "/fxml/TeacherProfileView.fxml"); break;
            case STUDENT_DASHBOARD: ViewLoader.loadFXMLInto(contentArea, "/fxml/StudentDashboardView.fxml"); break;
            case STUDENT_PROFILE: ViewLoader.loadFXMLInto(contentArea, "/fxml/StudentProfileView.fxml"); break;
            case STUDENT_SCORES: ViewLoader.loadFXMLInto(contentArea, "/fxml/StudentScoresView.fxml"); break;
            case STUDENT_ANALYSIS: ViewLoader.loadFXMLInto(contentArea, "/fxml/StudentAnalysisView.fxml"); break;
            case STUDENT_DIAGNOSIS: ViewLoader.loadFXMLInto(contentArea, "/fxml/StudentDiagnosisView.fxml"); break;

            case STUDENT_COMMENT: ViewLoader.loadFXMLInto(contentArea, "/fxml/StudentCommentView.fxml"); break;
            case STUDENT_RISK: ViewLoader.loadFXMLInto(contentArea, "/fxml/StudentRiskView.fxml"); break;
            default: ViewLoader.loadFXMLInto(contentArea, "/fxml/AdminDashboardView.fxml"); break;
        }
    }

    @FXML
    private void handleLogout() {
        UserSession.getInstance().logout();
        ViewLoader.loadScene("/fxml/LoginView.fxml", "AI智能校园学情分析系统 - 登录");
    }
}
