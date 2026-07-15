package com.aicampus.controller;

import com.aicampus.model.PageResult;
import com.aicampus.model.RiskWarning;
import com.aicampus.model.Student;
import com.aicampus.service.RiskWarningService;
import com.aicampus.service.StudentService;
import com.aicampus.session.UserSession;
import com.aicampus.util.ViewLoader;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class TeacherDashboardController {

    @FXML private Label valueStudents;
    @FXML private Label valueWarnings;
    @FXML private Label valueUsername;

    @FXML
    public void initialize() {
        valueUsername.setText(UserSession.getInstance().getUsername());
        loadData();
    }

    private void loadData() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                PageResult<Student> studentRes = StudentService.getPage(1, 1);
                PageResult<RiskWarning> riskRes = RiskWarningService.getRiskWarningPage(1, 1, "UNHANDLED");
                Platform.runLater(() -> {
                    valueStudents.setText(String.valueOf(studentRes.getTotal()));
                    valueWarnings.setText(String.valueOf(riskRes.getTotal()));
                });
                return null;
            }
        };

        task.setOnFailed(event -> {
            // Silently handle
        });

        new Thread(task).start();
    }

    @FXML private void goToScoreEntry() { navigateToWebView("/teacher/score/entry"); }
    @FXML private void goToScoreImport() { navigateToWebView("/teacher/score/import"); }
    @FXML private void goToDiagnosis() { navigateToWebView("/teacher/diagnosis"); }
    @FXML private void goToComment() { navigateToWebView("/teacher/comment"); }

    private void navigateToWebView(String route) {
        javafx.scene.Node node = valueStudents;
        while (node != null) {
            if (node instanceof BorderPane bp) {
                if (bp.getCenter() != null || bp.getLeft() == null) {
                    ViewLoader.loadWebView(bp, route);
                    return;
                }
            }
            node = node.getParent();
        }
    }
}
