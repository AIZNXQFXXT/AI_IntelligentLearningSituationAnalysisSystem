package com.aicampus.controller;

import com.aicampus.service.SemesterService;
import com.aicampus.service.StatsService;
import com.aicampus.util.AppExecutors;
import com.aicampus.util.CrudHelper;
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

    private String currentSemester;

    @FXML
    public void initialize() {
        loadTrend();
        loadSemester();
    }

    private void loadSemester() {
        Task<List<String>> task = new Task<>() {
            @Override protected List<String> call() throws Exception {
                return SemesterService.getAllSemesters();
            }
        };
        task.setOnSucceeded(e -> {
            List<String> semesters = task.getValue();
            if (semesters != null && !semesters.isEmpty()) {
                currentSemester = semesters.get(0);
            }
            loadRadar();
        });
        task.setOnFailed(e -> loadRadar());
        AppExecutors.submit(task::run);
    }

    private void loadTrend() {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override protected List<Map<String, Object>> call() throws Exception {
                return StatsService.getStudentTrend();
            }
        };
        task.setOnSucceeded(e -> {
            trendChart.getData().clear();
            List<Map<String, Object>> list = task.getValue();
            if (list == null || list.isEmpty()) return;
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("平均分");
            for (Map<String, Object> item : list) {
                String semester = (String) item.get("semester");
                if (semester == null) continue;
                series.getData().add(new XYChart.Data<>(
                    semester,
                    ((Number) item.get("avgScore")).doubleValue()
                ));
            }
            trendChart.getData().add(series);
        });
        task.setOnFailed(e -> CrudHelper.showError("加载成绩趋势失败"));
        AppExecutors.submit(task::run);
    }

    private void loadRadar() {
        if (currentSemester == null) return;
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override protected List<Map<String, Object>> call() throws Exception {
                return StatsService.getStudentRadar(currentSemester);
            }
        };
        task.setOnSucceeded(e -> {
            radarChart.getData().clear();
            List<Map<String, Object>> list = task.getValue();
            if (list == null || list.isEmpty()) return;
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("各课程成绩");
            for (Map<String, Object> item : list) {
                String courseName = (String) item.get("courseName");
                if (courseName == null) continue;
                series.getData().add(new XYChart.Data<>(
                    courseName,
                    ((Number) item.get("finalScore")).doubleValue()
                ));
            }
            radarChart.getData().add(series);
        });
        task.setOnFailed(e -> CrudHelper.showError("加载课程成绩失败"));
        AppExecutors.submit(task::run);
    }

}
