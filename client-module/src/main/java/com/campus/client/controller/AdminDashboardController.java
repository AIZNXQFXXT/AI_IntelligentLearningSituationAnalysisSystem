package com.campus.client.controller;

import com.campus.common.vo.DashboardVO;
import com.campus.client.service.StatsService;
import com.campus.client.util.AppExecutors;
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
        Task<DashboardVO> task = new Task<>() {
            @Override
            protected DashboardVO call() throws Exception {
                return StatsService.getSchoolOverview();
            }
        };

        task.setOnSucceeded(event -> {
            DashboardVO data = task.getValue();
            if (data != null) {
                valueClasses.setText(String.valueOf(data.getClassCount()));
                valueTeachers.setText(String.valueOf(data.getTeacherCount()));
                valueStudents.setText(String.valueOf(data.getStudentCount()));
                valueCourses.setText(String.valueOf(data.getCourseCount()));
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
