package com.aicampus.controller;

import com.aicampus.model.ClassInfo;
import com.aicampus.model.Course;
import com.aicampus.model.ScoreDistribution;
import com.aicampus.model.SchoolStats;
import com.aicampus.service.ClassService;
import com.aicampus.service.CourseService;
import com.aicampus.service.StatsService;
import com.aicampus.util.CrudHelper;
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
        new Thread(task).start();
    }

    private void loadCourses() {
        Task<List<Course>> task = new Task<>() {
            @Override protected List<Course> call() throws Exception { return CourseService.getAll(); }
        };
        task.setOnSucceeded(e -> courseCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        new Thread(task).start();
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
        loadTrend(cls.getId());
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
            passRate.setText(String.format("%.1f%%", s.getPassRate() * 100));
            excellentRate.setText(String.format("%.1f%%", s.getExcellentRate() * 100));
            studentCount.setText(String.valueOf(s.getStudentCount()));
        });
        new Thread(task).start();
    }

    private void loadDistribution(int classId, int courseId) {
        Task<List<ScoreDistribution>> task = new Task<>() {
            @Override protected List<ScoreDistribution> call() throws Exception {
                return StatsService.getScoreDistribution(classId, courseId);
            }
        };
        task.setOnSucceeded(e -> {
            distributionChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("人数");
            for (ScoreDistribution item : task.getValue()) {
                series.getData().add(new XYChart.Data<>(item.getRange(), item.getCount()));
            }
            distributionChart.getData().add(series);
        });
        new Thread(task).start();
    }

    @SuppressWarnings("unchecked")
    private void loadTrend(int classId) {
        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override protected List<Map<String, Object>> call() throws Exception {
                return StatsService.getTrend(classId);
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
        new Thread(task).start();
    }
}
