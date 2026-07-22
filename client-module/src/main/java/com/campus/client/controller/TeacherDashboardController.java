package com.aicampus.controller;

import com.aicampus.model.SchoolOverviewVO;
import com.aicampus.service.StatsService;
import com.aicampus.session.UserSession;
import com.aicampus.util.AppExecutors;
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
                SchoolOverviewVO overview = StatsService.getSchoolAcademicOverview();
                return overview != null ? overview.getTotalStudents() : 0L;
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
