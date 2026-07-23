package com.campus.client.controller;

import com.campus.client.service.SemesterService;
import com.campus.client.service.StatsService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
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

    @FXML private ComboBox<String> semesterFilter;
    @FXML private Label maxSubjectLabel;
    @FXML private Label minSubjectLabel;

    @FXML
    public void initialize() {
        loadTrend();
        loadSemesters();
    }

    private void loadSemesters() {
        Task<List<String>> task = new Task<>() {
            @Override protected List<String> call() throws Exception {
                return SemesterService.getAllSemesters();
            }
        };
        task.setOnSucceeded(e -> {
            List<String> semesters = task.getValue();
            semesterFilter.setItems(FXCollections.observableArrayList(semesters));
            semesterFilter.setOnAction(ev -> handleFilterChange());
            if (semesters != null && !semesters.isEmpty()) {
                semesterFilter.getSelectionModel().selectFirst();
            }
        });
        task.setOnFailed(e -> {});
        AppExecutors.submit(task::run);
    }

    private void handleFilterChange() {
        String semester = semesterFilter.getValue();
        if (semester != null) {
            loadRadar(semester);
        }
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
            series.setName("绩点");
            for (Map<String, Object> item : list) {
                String semester = (String) item.get("semester");
                if (semester == null) continue;
                Number gpa = (Number) item.get("gpa");
                if (gpa == null) continue;
                series.getData().add(new XYChart.Data<>(
                    semester,
                    gpa.doubleValue()
                ));
            }
            trendChart.getData().add(series);
        });
        task.setOnFailed(e -> CrudHelper.showError("加载GPA趋势失败"));
        AppExecutors.submit(task::run);
    }

    private void loadRadar(String semester) {
        if (semester == null) return;
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override protected List<Map<String, Object>> call() throws Exception {
                return StatsService.getStudentRadar(semester);
            }
        };
        task.setOnSucceeded(e -> {
            radarChart.getData().clear();
            List<Map<String, Object>> list = task.getValue();
            if (list == null || list.isEmpty()) {
                maxSubjectLabel.setText("--");
                minSubjectLabel.setText("--");
                return;
            }
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("各课程成绩");
            String maxSubject = null, minSubject = null;
            double maxScore = -1, minScore = 101;
            for (Map<String, Object> item : list) {
                String courseName = (String) item.get("courseName");
                if (courseName == null) continue;
                double score = ((Number) item.get("finalScore")).doubleValue();
                series.getData().add(new XYChart.Data<>(courseName, score));
                if (score > maxScore) { maxScore = score; maxSubject = courseName; }
                if (score < minScore) { minScore = score; minSubject = courseName; }
            }
            radarChart.getData().add(series);
            maxSubjectLabel.setText(maxSubject + "  " + (int) maxScore);
            minSubjectLabel.setText(minSubject + "  " + (int) minScore);
        });
        task.setOnFailed(e -> CrudHelper.showError("加载课程成绩失败"));
        AppExecutors.submit(task::run);
    }

}
