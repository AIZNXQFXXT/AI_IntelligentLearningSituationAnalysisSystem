package com.campus.client.controller;

import com.campus.client.model.PageResult;
import com.campus.client.model.Student;
import com.campus.client.service.StatsService;
import com.campus.client.service.StudentService;
import com.campus.client.session.UserSession;
import com.campus.client.util.AppExecutors;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Map;

public class TeacherDashboardController {

    @FXML private Label valueStudents;
    @FXML private Label valueWarnings;
    @FXML private Label valueUsername;

    @FXML
    public void initialize() {
        valueUsername.setText(UserSession.getInstance().getUsername());
        loadStudentCount();
        loadWarningCount();
    }

    private void loadStudentCount() {
        Task<Long> task = new Task<>() {
            @Override protected Long call() throws Exception {
                // 后端对 TEACHER 角色按本班并集 (班主任 ∪ 教学任务) 过滤, total 即本班学生总数
                PageResult<Student> pr = StudentService.getPage(1, 1, null, null);
                return pr != null ? (long) pr.getTotal() : 0L;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() ->
                valueStudents.setText(String.valueOf(task.getValue()))));
        task.setOnFailed(e -> Platform.runLater(() ->
                valueStudents.setText("0")));
        AppExecutors.submit(task::run);
    }

    private void loadWarningCount() {
        Task<Long> task = new Task<>() {
            @Override protected Long call() throws Exception {
                Map<String, Object> summary = StatsService.getRiskSummary();
                Object count = summary != null ? summary.get("unhandledCount") : 0;
                return count instanceof Number ? ((Number) count).longValue() : 0L;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() ->
                valueWarnings.setText(String.valueOf(task.getValue()))));
        task.setOnFailed(e -> Platform.runLater(() ->
                valueWarnings.setText("0")));
        AppExecutors.submit(task::run);
    }

    @FXML private void goToScoreEntry() { navigateTo("/teacher/score/entry"); }
    @FXML private void goToScoreImport() { navigateTo("/teacher/score/import"); }
    @FXML private void goToDiagnosis() { navigateTo("/teacher/diagnosis"); }
    @FXML private void goToComment() { navigateTo("/teacher/comment"); }

    private void navigateTo(String route) {
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.navigateTo(route);
        }
    }
}
