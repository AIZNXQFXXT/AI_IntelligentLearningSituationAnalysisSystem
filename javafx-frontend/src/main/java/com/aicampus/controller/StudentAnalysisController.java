package com.aicampus.controller;

import com.aicampus.service.StatsService;
import com.aicampus.session.UserSession;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

import java.util.List;
import java.util.Map;

public class StudentAnalysisController {

    @FXML private LineChart<String, Number> trendChart;
    @FXML private CategoryAxis trendXAxis;
    @FXML private NumberAxis trendYAxis;

    @FXML private BarChart<String, Number> radarChart;
    @FXML private CategoryAxis radarXAxis;
    @FXML private NumberAxis radarYAxis;

    @FXML
    public void initialize() {
        loadTrend();
        loadRadar();
    }

    private void loadTrend() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override protected List<Map<String, Object>> call() throws Exception {
                return StatsService.getStudentTrend(getCurrentUserId());
            }
        };
        task.setOnSucceeded(e -> {
            trendChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("平均分");
            for (Map<String, Object> item : task.getValue()) {
                series.getData().add(new XYChart.Data<>(
                    (String) item.get("semester"),
                    ((Number) item.get("avgScore")).doubleValue()
                ));
            }
            trendChart.getData().add(series);
        });
        task.setOnFailed(e -> CrudHelper.showError("加载成绩趋势失败"));
        new Thread(task).start();
    }

    private void loadRadar() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override protected List<Map<String, Object>> call() throws Exception {
                return StatsService.getStudentRadar(getCurrentUserId());
            }
        };
        task.setOnSucceeded(e -> {
            radarChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("各课程成绩");
            for (Map<String, Object> item : task.getValue()) {
                series.getData().add(new XYChart.Data<>(
                    (String) item.get("courseName"),
                    ((Number) item.get("score")).doubleValue()
                ));
            }
            radarChart.getData().add(series);
        });
        task.setOnFailed(e -> CrudHelper.showError("加载课程成绩失败"));
        new Thread(task).start();
    }

    private int getCurrentUserId() {
        return UserSession.getInstance().getUserId();
    }
}
