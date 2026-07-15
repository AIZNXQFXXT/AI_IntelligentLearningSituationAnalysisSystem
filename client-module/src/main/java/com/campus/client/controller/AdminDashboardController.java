package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.service.StatsService;
import com.campus.client.util.AlertHelper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label totalStudents;
    @FXML private Label totalTeachers;
    @FXML private Label totalClasses;
    @FXML private Label totalCourses;
    @FXML private Label highRiskCount;
    @FXML private Label midRiskCount;
    @FXML private Label pendingWarnings;
    @FXML private Button refreshButton;

    private final StatsService statsService = new StatsService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadData();
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    private void loadData() {
        refreshButton.setDisable(true);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    ApiResult<Map<String, Object>> overview = statsService.getSchoolOverview();
                    if (overview.isSuccess() && overview.getData() != null) {
                        Map<String, Object> data = overview.getData();
                        Platform.runLater(() -> {
                            setTextIfPresent(totalStudents, data, "totalStudents");
                            setTextIfPresent(totalTeachers, data, "totalTeachers");
                            setTextIfPresent(totalClasses, data, "totalClasses");
                            setTextIfPresent(totalCourses, data, "totalCourses");
                        });
                    }
                } catch (Exception e) {
                }
                try {
                    ApiResult<Map<String, Object>> risk = statsService.getRiskSummary();
                    if (risk.isSuccess() && risk.getData() != null) {
                        Map<String, Object> data = risk.getData();
                        Platform.runLater(() -> {
                            setTextIfPresent(highRiskCount, data, "highRisk");
                            setTextIfPresent(midRiskCount, data, "mediumRisk");
                            setTextIfPresent(pendingWarnings, data, "pendingWarnings");
                        });
                    }
                } catch (Exception e) {
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> refreshButton.setDisable(false));
        task.setOnFailed(e -> refreshButton.setDisable(false));
        new Thread(task).start();
    }

    private void setTextIfPresent(Label label, Map<String, Object> data, String key) {
        if (data.containsKey(key) && data.get(key) != null) {
            label.setText(String.valueOf(data.get(key)));
        }
    }
}
