package com.aicampus.controller;

import com.aicampus.model.SchoolOverview;
import com.aicampus.service.StatsService;
import com.aicampus.util.ViewLoader;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class AdminDashboardController {

    @FXML private Label valueClasses;
    @FXML private Label valueTeachers;
    @FXML private Label valueStudents;
    @FXML private Label valueCourses;
    @FXML private Label failCountLabel;
    @FXML private Label avgScoreLabel;
    @FXML private Label passRateLabel;

    @FXML
    public void initialize() {
        loadOverview();
    }

    private void loadOverview() {
        Task<SchoolOverview> task = new Task<>() {
            @Override
            protected SchoolOverview call() throws Exception {
                return StatsService.getSchoolOverview();
            }
        };

        task.setOnSucceeded(event -> {
            SchoolOverview data = task.getValue();
            if (data != null) {
                valueClasses.setText(String.valueOf(data.getTotalClasses()));
                valueTeachers.setText(String.valueOf(data.getTotalTeachers()));
                valueStudents.setText(String.valueOf(data.getTotalStudents()));
                valueCourses.setText(String.valueOf(data.getTotalCourses()));
                failCountLabel.setText(data.getFailCount() + " 人");
                avgScoreLabel.setText(String.format("%.1f", data.getSchoolAvgScore()));
                passRateLabel.setText(String.format("%.1f%%", data.getSchoolPassRate() * 100));
            }
        });

        task.setOnFailed(event -> {
            // Silently handle — dashboard shows "--" defaults
        });

        new Thread(task).start();
    }

    private void navigateToRoute(String route) {
        BorderPane contentArea = (BorderPane) getView().getParent().getParent();
        // If parent is not a BorderPane, walk up
        if (contentArea == null) return;
        ViewLoader.loadWebView(contentArea, route);
    }

    private VBox getView() {
        return (VBox) valueClasses.getScene().getRoot().lookup(".content-area");
        // Fallback: navigate via MainLayout
    }

    @FXML private void goToClasses() { navigateToWebView("/admin/classes"); }
    @FXML private void goToTeachers() { navigateToWebView("/admin/teachers"); }
    @FXML private void goToStudents() { navigateToWebView("/admin/students"); }
    @FXML private void goToTeachingTasks() { navigateToWebView("/admin/teaching-tasks"); }

    private void navigateToWebView(String route) {
        // Find the content BorderPane by traversing up to MainLayout
        javafx.scene.Node node = valueClasses;
        while (node != null) {
            if (node instanceof BorderPane bp && bp.getId() == null) {
                // This is the content area
                ViewLoader.loadWebView(bp, route);
                return;
            }
            node = node.getParent();
        }
    }
}
