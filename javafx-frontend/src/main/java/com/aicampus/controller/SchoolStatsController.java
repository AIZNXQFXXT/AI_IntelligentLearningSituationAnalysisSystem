package com.aicampus.controller;

import com.aicampus.model.*;
import com.aicampus.service.ClassService;
import com.aicampus.service.CourseService;
import com.aicampus.service.StatsService;
import com.aicampus.util.CrudHelper;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.List;

public class SchoolStatsController {

    @FXML private Label valueAvgScore;
    @FXML private Label valuePassRate;
    @FXML private Label valueFailCount;
    @FXML private Label valueTotalStudents;
    @FXML private ComboBox<String> classFilter;
    @FXML private ComboBox<String> courseFilter;
    @FXML private BarChart<String, Number> chart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private List<ClassInfo> classList;
    private List<Course> courseList;

    @FXML
    public void initialize() {
        classFilter.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> loadChart());
        courseFilter.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> loadChart());
        loadOverview();
        loadFilters();
    }

    private void loadOverview() {
        Task<SchoolOverview> task = new Task<>() {
            @Override
            protected SchoolOverview call() throws Exception { return StatsService.getSchoolOverview(); }
        };
        task.setOnSucceeded(e -> {
            SchoolOverview ov = task.getValue();
            valueAvgScore.setText(String.format("%.1f", ov.getSchoolAvgScore()));
            valuePassRate.setText(String.format("%.1f%%", ov.getSchoolPassRate()));
            valueFailCount.setText(String.valueOf(ov.getFailCount()));
            valueTotalStudents.setText(String.valueOf(ov.getTotalStudents()));
        });
        new Thread(task).start();
    }

    private void loadFilters() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                classList = ClassService.getAll();
                courseList = CourseService.getAll();
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            for (ClassInfo c : classList) classFilter.getItems().add(c.getId() + " - " + c.getClassName());
            for (Course c : courseList) courseFilter.getItems().add(c.getId() + " - " + c.getName());
        });
        new Thread(task).start();
    }

    private void loadChart() {
        int classIdx = classFilter.getSelectionModel().getSelectedIndex();
        int courseIdx = courseFilter.getSelectionModel().getSelectedIndex();
        if (classIdx < 0 || courseIdx < 0 || classList == null || courseList == null) return;

        int classId = classList.get(classIdx).getId();
        int courseId = courseList.get(courseIdx).getId();

        Task<List<ScoreDistribution>> task = new Task<>() {
            @Override
            protected List<ScoreDistribution> call() throws Exception {
                return StatsService.getScoreDistribution(classId, courseId);
            }
        };
        task.setOnSucceeded(e -> {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (ScoreDistribution d : task.getValue()) {
                series.getData().add(new XYChart.Data<>(d.getRange(), d.getCount()));
            }
            chart.getData().clear();
            chart.getData().add(series);
        });
        task.setOnFailed(e -> CrudHelper.showError("加载图表失败"));
        new Thread(task).start();
    }
}
