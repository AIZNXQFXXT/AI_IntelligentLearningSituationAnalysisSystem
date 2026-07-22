package com.aicampus.controller;

import com.aicampus.model.*;
import com.aicampus.service.*;
import com.aicampus.util.AppExecutors;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

public class ReportExportController {

    @FXML private ComboBox<String> exportTypeCombo;
    @FXML private ComboBox<ClassInfo> classCombo;
    @FXML private ComboBox<Course> courseCombo;
    @FXML private ComboBox<Exam> examCombo;
    @FXML private ComboBox<String> riskLevelCombo;
    @FXML private HBox scoreOptions;
    @FXML private HBox riskOptions;
    @FXML private Label resultLabel;

    @FXML
    public void initialize() {
        exportTypeCombo.setItems(FXCollections.observableArrayList(Arrays.asList("成绩表", "评语汇总", "高风险学生")));
        exportTypeCombo.getSelectionModel().select(1);
        exportTypeCombo.setOnAction(e -> updateOptionsVisibility());

        riskLevelCombo.setItems(FXCollections.observableArrayList(Arrays.asList("HIGH", "MEDIUM", "LOW")));
        riskLevelCombo.getSelectionModel().selectFirst();

        loadClasses();
        loadCourses();
        loadExams();
    }

    private void updateOptionsVisibility() {
        String type = exportTypeCombo.getValue();
        scoreOptions.setVisible("成绩表".equals(type));
        scoreOptions.setManaged("成绩表".equals(type));
        riskOptions.setVisible("高风险学生".equals(type));
        riskOptions.setManaged("高风险学生".equals(type));
    }

    private void loadClasses() {
        Task<List<ClassInfo>> task = new Task<>() {
            @Override protected List<ClassInfo> call() throws Exception { return ClassService.getAll(); }
        };
        task.setOnSucceeded(e -> classCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> CrudHelper.showError("加载班级列表失败"));
        AppExecutors.submit(task::run);
    }

    private void loadCourses() {
        Task<List<Course>> task = new Task<>() {
            @Override protected List<Course> call() throws Exception { return CourseService.getAll(); }
        };
        task.setOnSucceeded(e -> courseCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> CrudHelper.showError("加载课程列表失败"));
        AppExecutors.submit(task::run);
    }

    private void loadExams() {
        Task<List<Exam>> task = new Task<>() {
            @Override protected List<Exam> call() throws Exception { return ExamService.getPage(1, 100, null).getRecords(); }
        };
        task.setOnSucceeded(e -> examCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> CrudHelper.showError("加载考试列表失败"));
        AppExecutors.submit(task::run);
    }

    @FXML
    private void handleExport() {
        String type = exportTypeCombo.getValue();
        switch (type) {
            case "成绩表": exportScores(); break;
            case "评语汇总": exportComments(); break;
            case "高风险学生": exportRisks(); break;
        }
    }

    private void exportScores() {
        ClassInfo cls = classCombo.getValue();
        Exam exam = examCombo.getValue();
        Course course = courseCombo.getValue();
        if (exam == null || course == null) {
            CrudHelper.showAlert("请选择考试和课程");
            return;
        }

        Task<byte[]> task = new Task<>() {
            @Override protected byte[] call() throws Exception {
                String path = "/reports/excel/score-table?examId=" + exam.getId() + "&courseId=" + course.getId();
                if (cls != null) path += "&classId=" + cls.getId();
                return ApiClient.downloadBytes(path);
            }
        };
        task.setOnSucceeded(e -> {
            try {
                File file = showSaveDialog("成绩表.xlsx");
                if (file == null) return;
                try (FileOutputStream fos = new FileOutputStream(file)) { fos.write(task.getValue()); }
                resultLabel.setText("导出成功: " + file.getAbsolutePath());
                resultLabel.setVisible(true);
            } catch (Exception ex) {
                CrudHelper.showError("导出失败: " + ex.getMessage());
            }
        });
        task.setOnFailed(e -> CrudHelper.showError("导出失败: " + task.getException().getMessage()));
        AppExecutors.submit(task::run);
    }

    private void exportComments() {
        Task<byte[]> task = new Task<>() {
            @Override protected byte[] call() throws Exception {
                return ApiClient.downloadBytes("/reports/excel/comments");
            }
        };
        task.setOnSucceeded(e -> {
            try {
                File file = showSaveDialog("评语汇总.xlsx");
                if (file == null) return;
                try (FileOutputStream fos = new FileOutputStream(file)) { fos.write(task.getValue()); }
                resultLabel.setText("导出成功: " + file.getAbsolutePath());
                resultLabel.setVisible(true);
            } catch (Exception ex) {
                CrudHelper.showError("导出失败: " + ex.getMessage());
            }
        });
        task.setOnFailed(e -> CrudHelper.showError("导出失败: " + task.getException().getMessage()));
        AppExecutors.submit(task::run);
    }

    private void exportRisks() {
        String riskLevel = riskLevelCombo.getValue();

        Task<byte[]> task = new Task<>() {
            @Override protected byte[] call() throws Exception {
                String path = "/reports/excel/risk-list";
                if (riskLevel != null) path += "?riskLevel=" + riskLevel;
                return ApiClient.downloadBytes(path);
            }
        };
        task.setOnSucceeded(e -> {
            try {
                File file = showSaveDialog("高风险学生.xlsx");
                if (file == null) return;
                try (FileOutputStream fos = new FileOutputStream(file)) { fos.write(task.getValue()); }
                resultLabel.setText("导出成功: " + file.getAbsolutePath());
                resultLabel.setVisible(true);
            } catch (Exception ex) {
                CrudHelper.showError("导出失败: " + ex.getMessage());
            }
        });
        task.setOnFailed(e -> CrudHelper.showError("导出失败: " + task.getException().getMessage()));
        AppExecutors.submit(task::run);
    }

    private File showSaveDialog(String defaultName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存报表");
        fileChooser.setInitialFileName(defaultName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        return fileChooser.showSaveDialog(null);
    }
}
