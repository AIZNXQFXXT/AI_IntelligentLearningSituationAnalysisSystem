package com.campus.client.controller;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.ClassInfo;
import com.campus.client.service.ApiClient;
import com.campus.client.service.ClassService;
import com.campus.client.util.AppExecutors;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Map;

public class SubjectAnalyticsController {

    @FXML private ComboBox<String> gradeFilter;
    @FXML private Label valueAvgScore;
    @FXML private Label valuePassRate;
    @FXML private Label valueCourseCount;
    @FXML private BarChart<String, Number> chart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private TableView<Map<String, Object>> dataTable;
    @FXML private TableColumn<Map<String, Object>, String> colCourseName;
    @FXML private TableColumn<Map<String, Object>, Number> colAvgScore;
    @FXML private TableColumn<Map<String, Object>, Number> colPassRate;
    @FXML private TableColumn<Map<String, Object>, Number> colExcellentRate;
    @FXML private TableColumn<Map<String, Object>, Number> colStudentCount;
    @FXML private Label noDataLabel;

    private final ObservableList<Map<String, Object>> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colCourseName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(data.getValue().getOrDefault("courseName", ""))));
        colAvgScore.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                Double.parseDouble(String.valueOf(data.getValue().getOrDefault("avgScore", 0)))));
        colPassRate.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                Double.parseDouble(String.valueOf(data.getValue().getOrDefault("passRate", 0)))));
        colExcellentRate.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                Double.parseDouble(String.valueOf(data.getValue().getOrDefault("excellentRate", 0)))));
        colStudentCount.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
                Integer.parseInt(String.valueOf(data.getValue().getOrDefault("studentCount", 0)))));
        dataTable.setItems(tableData);

        loadGrades();
    }

    private void loadGrades() {
        Task<List<ClassInfo>> task = new Task<>() {
            @Override protected List<ClassInfo> call() throws Exception {
                return ClassService.getAll();
            }
        };
        task.setOnSucceeded(e -> {
            List<String> grades = task.getValue().stream()
                    .map(ClassInfo::getGrade)
                    .filter(g -> g != null && !g.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
            gradeFilter.getItems().addAll(grades);
        });
        AppExecutors.submit(task::run);
    }

    @FXML
    private void handleQuery() {
        String grade = gradeFilter.getSelectionModel().getSelectedItem();
        String gradeParam = grade != null ? ApiClient.encodeParam(grade) : "";

        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                String path = "/academic-stats/course-summary?grade=" + gradeParam;
                return ApiClient.get(path, new TypeReference<ApiResponse<List<Map<String, Object>>>>() {});
            }
        };
        task.setOnSucceeded(e -> {
            List<Map<String, Object>> result = task.getValue();
            tableData.clear();
            chart.getData().clear();

            if (result != null && !result.isEmpty()) {
                tableData.addAll(result);
                noDataLabel.setVisible(false);
                noDataLabel.setManaged(false);
                dataTable.setVisible(true);
                dataTable.setManaged(true);

                // Build bar chart
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                for (Map<String, Object> row : result) {
                    String name = String.valueOf(row.getOrDefault("courseName", ""));
                    double avg = Double.parseDouble(String.valueOf(row.getOrDefault("avgScore", 0)));
                    series.getData().add(new XYChart.Data<>(name, avg));
                }
                chart.getData().add(series);

                // Aggregate stats
                double totalAvg = result.stream()
                        .mapToDouble(m -> Double.parseDouble(String.valueOf(m.getOrDefault("avgScore", 0))))
                        .average().orElse(0);
                double totalPassRate = result.stream()
                        .mapToDouble(m -> Double.parseDouble(String.valueOf(m.getOrDefault("passRate", 0))))
                        .average().orElse(0);

                valueAvgScore.setText(String.format("%.1f", totalAvg));
                valuePassRate.setText(String.format("%.1f%%", totalPassRate));
                valueCourseCount.setText(String.valueOf(result.size()));
            } else {
                noDataLabel.setVisible(true);
                noDataLabel.setManaged(true);
                dataTable.setVisible(false);
                dataTable.setManaged(false);
                valueAvgScore.setText("--");
                valuePassRate.setText("--");
                valueCourseCount.setText("--");
            }
        });
        task.setOnFailed(e -> {
            tableData.clear();
            chart.getData().clear();
            noDataLabel.setVisible(true);
            noDataLabel.setManaged(true);
            dataTable.setVisible(false);
            dataTable.setManaged(false);
        });
        AppExecutors.submit(task::run);
    }
}
