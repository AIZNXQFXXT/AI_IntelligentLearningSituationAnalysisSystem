package com.campus.client.controller;

import com.campus.client.model.*;
import com.campus.client.service.ClassService;
import com.campus.client.service.CourseService;
import com.campus.client.service.StatsService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
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
        Task<SchoolOverviewVO> task = new Task<>() {
            @Override
            protected SchoolOverviewVO call() throws Exception {
                return StatsService.getSchoolAcademicOverview();
            }
        };
        task.setOnSucceeded(e -> {
            SchoolOverviewVO ov = task.getValue();
            long totalStudents = ov.getTotalStudents();
            valueAvgScore.setText(String.format("%.1f", ov.getAvgScore()));
            valuePassRate.setText(String.format("%.1f%%", ov.getPassRate()));
            valueFailCount.setText(String.valueOf((int) Math.round(totalStudents * ov.getFailRate() / 100.0)));
            valueTotalStudents.setText(String.valueOf(totalStudents));
        });
        task.setOnFailed(e -> CrudHelper.showError("加载全校概览失败"));
        AppExecutors.submit(task::run);
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
        AppExecutors.submit(task::run);
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
            chart.getData().clear();
            List<ScoreDistribution> list = task.getValue();
            if (list == null || list.isEmpty()) return;
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            for (ScoreDistribution d : list) {
                if (d.getRangeLabel() == null) continue;
                series.getData().add(new XYChart.Data<>(d.getRangeLabel(), d.getCount()));
            }
            chart.getData().add(series);
        });
        task.setOnFailed(e -> CrudHelper.showError("加载图表失败"));
        AppExecutors.submit(task::run);
    }
}
