package com.campus.client.controller;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.ClassInfo;
import com.campus.client.model.Course;
import com.campus.client.service.ApiClient;
import com.campus.client.service.ClassService;
import com.campus.client.service.CourseService;
import com.campus.client.util.AppExecutors;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Map;

public class GradeAnalyticsController {

    @FXML private ComboBox<String> gradeFilter;
    @FXML private ComboBox<String> courseFilter;
    @FXML private Label valueAvgScore;
    @FXML private Label valuePassRate;
    @FXML private Label valueFailCount;
    @FXML private Label valueTotalStudents;
    @FXML private TableView<Map<String, Object>> dataTable;
    @FXML private TableColumn<Map<String, Object>, String> colClassName;
    @FXML private TableColumn<Map<String, Object>, Number> colStudentCount;
    @FXML private TableColumn<Map<String, Object>, Number> colAvgScore;
    @FXML private TableColumn<Map<String, Object>, Number> colPassRate;
    @FXML private TableColumn<Map<String, Object>, Number> colMaxScore;
    @FXML private TableColumn<Map<String, Object>, Number> colMinScore;
    @FXML private Label noDataLabel;

    private final ObservableList<Map<String, Object>> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colClassName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(data.getValue().getOrDefault("className", ""))));
        colStudentCount.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
                Integer.parseInt(String.valueOf(data.getValue().getOrDefault("studentCount", 0)))));
        colAvgScore.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                Double.parseDouble(String.valueOf(data.getValue().getOrDefault("avgScore", 0)))));
        colPassRate.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                Double.parseDouble(String.valueOf(data.getValue().getOrDefault("passRate", 0)))));
        colMaxScore.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                Double.parseDouble(String.valueOf(data.getValue().getOrDefault("maxScore", 0)))));
        colMinScore.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(
                Double.parseDouble(String.valueOf(data.getValue().getOrDefault("minScore", 0)))));
        dataTable.setItems(tableData);

        loadFilters();
    }

    private void loadFilters() {
        loadGrades();
        Task<List<Course>> task = new Task<>() {
            @Override
            protected List<Course> call() throws Exception { return CourseService.getAll(); }
        };
        task.setOnSucceeded(e -> {
            for (Course c : task.getValue()) {
                courseFilter.getItems().add(c.getId() + " - " + c.getName());
            }
        });
        AppExecutors.submit(task::run);
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
        int courseIdx = courseFilter.getSelectionModel().getSelectedIndex();
        if (grade == null) {
            grade = "";
        }

        String courseParam = "";
        if (courseIdx >= 0) {
            String selected = courseFilter.getItems().get(courseIdx);
            String courseId = selected.split(" - ")[0];
            courseParam = "&courseId=" + courseId;
        }

        String finalGrade = grade;
        String finalCourseParam = courseParam;
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                String path = "/academic-stats/grade-summary?grade=" + ApiClient.encodeParam(finalGrade) + finalCourseParam;
                return ApiClient.get(path, new TypeReference<ApiResponse<List<Map<String, Object>>>>() {});
            }
        };
        task.setOnSucceeded(e -> {
            List<Map<String, Object>> result = task.getValue();
            tableData.clear();
            if (result != null && !result.isEmpty()) {
                tableData.addAll(result);
                noDataLabel.setVisible(false);
                noDataLabel.setManaged(false);
                dataTable.setVisible(true);
                dataTable.setManaged(true);

                // Calculate aggregate stats
                double totalAvg = result.stream()
                        .mapToDouble(m -> Double.parseDouble(String.valueOf(m.getOrDefault("avgScore", 0))))
                        .average().orElse(0);
                double totalPassRate = result.stream()
                        .mapToDouble(m -> Double.parseDouble(String.valueOf(m.getOrDefault("passRate", 0))))
                        .average().orElse(0);
                int totalStudents = result.stream()
                        .mapToInt(m -> Integer.parseInt(String.valueOf(m.getOrDefault("studentCount", 0))))
                        .sum();
                int totalFail = (int) (totalStudents * (1 - totalPassRate / 100));

                valueAvgScore.setText(String.format("%.1f", totalAvg));
                valuePassRate.setText(String.format("%.1f%%", totalPassRate));
                valueFailCount.setText(String.valueOf(totalFail));
                valueTotalStudents.setText(String.valueOf(totalStudents));
            } else {
                noDataLabel.setVisible(true);
                noDataLabel.setManaged(true);
                dataTable.setVisible(false);
                dataTable.setManaged(false);
                valueAvgScore.setText("--");
                valuePassRate.setText("--");
                valueFailCount.setText("--");
                valueTotalStudents.setText("--");
            }
        });
        task.setOnFailed(e -> {
            tableData.clear();
            noDataLabel.setVisible(true);
            noDataLabel.setManaged(true);
            dataTable.setVisible(false);
            dataTable.setManaged(false);
        });
        AppExecutors.submit(task::run);
    }
}
