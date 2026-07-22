package com.aicampus.controller;

import com.aicampus.model.SchoolOverview;
import com.aicampus.service.StatsService;
import com.aicampus.util.AppExecutors;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AdminDashboardController {

    @FXML private Label valueClasses;
    @FXML private Label valueTeachers;
    @FXML private Label valueStudents;
    @FXML private Label valueCourses;

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
            }
        });

        task.setOnFailed(event -> {
        });

        AppExecutors.submit(task::run);
    }

    @FXML private void goToClasses() { navigateTo("/admin/classes"); }
    @FXML private void goToTeachers() { navigateTo("/admin/teachers"); }
    @FXML private void goToStudents() { navigateTo("/admin/students"); }
    @FXML private void goToTeachingTasks() { navigateTo("/admin/teaching-tasks"); }

    private void navigateTo(String route) {
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.navigateTo(route);
        }
    }
}
