package com.campus.client.controller;

import com.campus.client.model.ClassInfo;
import com.campus.client.model.Course;
import com.campus.client.model.CourseGrade;
import com.campus.client.model.GradePoint;
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
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.List;

public class ClassStatsController {

    @FXML private ComboBox<ClassInfo> classCombo;
    @FXML private ComboBox<Course> courseCombo;
    @FXML private Label avgGpa, maxGpa, minGpa, passRate, excellentRate, studentCount;

    @FXML private ScatterChart<String, Number> gradePointChart;
    @FXML private CategoryAxis gpXAxis;
    @FXML private NumberAxis gpYAxis;

    @FXML private BarChart<String, Number> courseGradeChart;
    @FXML private CategoryAxis cgXAxis;
    @FXML private NumberAxis cgYAxis;

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
        if (cls == null) {
            CrudHelper.showAlert("请选择班级");
            return;
        }
        Course course = courseCombo.getValue();
        Integer courseId = (course == null) ? null : course.getId();

        loadClassStats(cls.getId(), courseId);
        loadGradePoints(cls.getId(), courseId);
        loadCourseGrades(cls.getId());
    }

    private void loadClassStats(int classId, Integer courseId) {
        Task<SchoolStats> task = new Task<>() {
            @Override protected SchoolStats call() throws Exception {
                return StatsService.getClassStats(classId, courseId);
            }
        };
        task.setOnSucceeded(e -> {
            SchoolStats s = task.getValue();
            studentCount.setText(String.valueOf(s.getStudentCount()));
        });
        task.setOnFailed(e -> CrudHelper.showError("加载班级人数失败\n" + task.getException().getMessage()));
        AppExecutors.submit(task::run);
    }

    private void loadGradePoints(int classId, Integer courseId) {
        Task<List<GradePoint>> task = new Task<>() {
            @Override protected List<GradePoint> call() throws Exception {
                return StatsService.getGradePoints(classId, courseId);
            }
        };
        task.setOnSucceeded(e -> {
            gradePointChart.getData().clear();
            List<GradePoint> list = task.getValue();
            if (list == null || list.isEmpty()) {
                avgGpa.setText("--");
                maxGpa.setText("--");
                minGpa.setText("--");
                passRate.setText("0.0%");
                excellentRate.setText("0.0%");
                return;
            }
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("绩点");
            double sum = 0, max = Double.MIN_VALUE, min = Double.MAX_VALUE;
            int passed = 0, excellent = 0, valid = 0;
            for (GradePoint item : list) {
                if (item.getGpa() == null) continue;
                double gpa = item.getGpa();
                sum += gpa;
                max = Math.max(max, gpa);
                min = Math.min(min, gpa);
                if (gpa >= 1.0) passed++;
                if (gpa >= 4.0) excellent++;
                valid++;
                String label = item.getStudentName() != null ? item.getStudentName() : item.getStudentNo();
                series.getData().add(new XYChart.Data<>(label, gpa));
            }
            gradePointChart.getData().add(series);
            if (valid == 0) {
                avgGpa.setText("--");
                maxGpa.setText("--");
                minGpa.setText("--");
                passRate.setText("0.0%");
                excellentRate.setText("0.0%");
            } else {
                avgGpa.setText(String.format("%.2f", sum / valid));
                maxGpa.setText(String.format("%.2f", max));
                minGpa.setText(String.format("%.2f", min));
                passRate.setText(String.format("%.1f%%", passed * 100.0 / valid));
                excellentRate.setText(String.format("%.1f%%", excellent * 100.0 / valid));
            }
        });
        task.setOnFailed(e -> CrudHelper.showError("加载成绩点图失败\n" + task.getException().getMessage()));
        AppExecutors.submit(task::run);
    }

    private void loadCourseGrades(int classId) {
        Task<List<CourseGrade>> task = new Task<>() {
            @Override protected List<CourseGrade> call() throws Exception {
                return StatsService.getCourseGrades(classId);
            }
        };
        task.setOnSucceeded(e -> {
            courseGradeChart.getData().clear();
            List<CourseGrade> list = task.getValue();
            if (list == null || list.isEmpty()) return;
            XYChart.Series<String, Number> avgSeries = new XYChart.Series<>();
            avgSeries.setName("平均分");
            XYChart.Series<String, Number> maxSeries = new XYChart.Series<>();
            maxSeries.setName("最高分");
            XYChart.Series<String, Number> minSeries = new XYChart.Series<>();
            minSeries.setName("最低分");
            for (CourseGrade item : list) {
                if (item.getCourseName() == null) continue;
                String name = item.getCourseName();
                if (item.getAvgScore() != null) avgSeries.getData().add(new XYChart.Data<>(name, item.getAvgScore()));
                if (item.getMaxScore() != null) maxSeries.getData().add(new XYChart.Data<>(name, item.getMaxScore()));
                if (item.getMinScore() != null) minSeries.getData().add(new XYChart.Data<>(name, item.getMinScore()));
            }
            courseGradeChart.getData().add(avgSeries);
            courseGradeChart.getData().add(maxSeries);
            courseGradeChart.getData().add(minSeries);
        });
        task.setOnFailed(e -> CrudHelper.showError("加载课程成绩图失败\n" + task.getException().getMessage()));
        AppExecutors.submit(task::run);
    }
}
