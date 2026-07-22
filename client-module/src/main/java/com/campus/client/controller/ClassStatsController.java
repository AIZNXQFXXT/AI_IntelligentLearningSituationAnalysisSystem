package com.campus.client.controller;

import com.campus.client.model.ClassInfo;
import com.campus.client.model.Course;
import com.campus.client.model.ScoreDistribution;
import com.campus.client.model.SchoolStats;
import com.campus.client.service.ClassService;
import com.campus.client.service.CourseService;
import com.campus.client.service.StatsService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.List;
import java.util.Map;

public class ClassStatsController {

    @FXML private ComboBox<ClassInfo> classCombo;
    @FXML private ComboBox<Course> courseCombo;
    @FXML private Label avgScore, maxScore, minScore, passRate, excellentRate, studentCount;

    @FXML private BarChart<String, Number> distributionChart;
    @FXML private CategoryAxis distXAxis;
    @FXML private NumberAxis distYAxis;

    @FXML private LineChart<String, Number> trendChart;
    @FXML private CategoryAxis trendXAxis;
    @FXML private NumberAxis trendYAxis;

    @FXML
    public void initialize() {
        loadClasses();
        loadCourses();
    }

    private void loadClasses() {
        Task<List<ClassInfo>> task = new Task<>() {
            @Override protected List<ClassInfo> call() throws Exception { return ClassService.getAll(); }
        };
        task.setOnSucceeded(e -> classCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> CrudHelper.showError("加载班级列表失败\n" + task.getException().getMessage()));
        AppExecutors.submit(task::run);
    }

    private void loadCourses() {
        Task<List<Course>> task = new Task<>() {
            @Override protected List<Course> call() throws Exception { return CourseService.getAll(); }
        };
        task.setOnSucceeded(e -> courseCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> CrudHelper.showError("加载课程列表失败\n" + task.getException().getMessage()));
        AppExecutors.submit(task::run);
    }

    @FXML
    private void handleLoadStats() {
        ClassInfo cls = classCombo.getValue();
        Course course = courseCombo.getValue();
        if (cls == null || course == null) {
            CrudHelper.showAlert("请选择班级和课程");
            return;
        }

        loadClassStats(cls.getId(), course.getId());
        loadDistribution(cls.getId(), course.getId());
        loadTrend(cls.getId(), course.getId());
    }

    private void loadClassStats(int classId, int courseId) {
        Task<SchoolStats> task = new Task<>() {
            @Override protected SchoolStats call() throws Exception {
                return StatsService.getClassStats(classId, courseId);
            }
        };
        task.setOnSucceeded(e -> {
            SchoolStats s = task.getValue();
            avgScore.setText(String.format("%.1f", s.getAvgScore()));
            maxScore.setText(String.format("%.1f", s.getMaxScore()));
            minScore.setText(String.format("%.1f", s.getMinScore()));
            if (s.getStudentCount() == 0) {
                passRate.setText("0.0%");
                excellentRate.setText("0.0%");
            } else {
                passRate.setText(String.format("%.1f%%", s.getPassRate()));
                double excRate = (double) s.getExcellentCount() / s.getStudentCount() * 100;
                excellentRate.setText(String.format("%.1f%%", excRate));
            }
            studentCount.setText(String.valueOf(s.getStudentCount()));
        });
        task.setOnFailed(e -> CrudHelper.showError("加载班级统计失败\n" + task.getException().getMessage()));
        AppExecutors.submit(task::run);
    }

    private void loadDistribution(int classId, int courseId) {
        Task<List<ScoreDistribution>> task = new Task<>() {
            @Override protected List<ScoreDistribution> call() throws Exception {
                return StatsService.getScoreDistribution(classId, courseId);
            }
        };
        task.setOnSucceeded(e -> {
            distributionChart.getData().clear();
            List<ScoreDistribution> list = task.getValue();
            if (list == null || list.isEmpty()) return;
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("人数");
            for (ScoreDistribution item : list) {
                if (item.getRange() == null) continue;
                series.getData().add(new XYChart.Data<>(item.getRange(), item.getCount()));
            }
            distributionChart.getData().add(series);
        });
        task.setOnFailed(e -> CrudHelper.showError("加载成绩分布失败\n" + task.getException().getMessage()));
        AppExecutors.submit(task::run);
    }

    @SuppressWarnings("unchecked")
    private void loadTrend(int classId, int courseId) {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override protected List<Map<String, Object>> call() throws Exception {
                return StatsService.getTrend(classId, courseId);
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
        task.setOnFailed(e -> CrudHelper.showError("加载学期趋势失败\n" + task.getException().getMessage()));
        AppExecutors.submit(task::run);
    }
}
