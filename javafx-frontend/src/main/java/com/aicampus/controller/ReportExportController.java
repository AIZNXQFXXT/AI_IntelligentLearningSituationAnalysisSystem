package com.aicampus.controller;

import com.aicampus.model.*;
import com.aicampus.service.*;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

public class ReportExportController {

    @FXML private ComboBox<String> exportTypeCombo;
    @FXML private ComboBox<ClassInfo> classCombo;
    @FXML private ComboBox<Exam> examCombo;
    @FXML private ComboBox<String> riskLevelCombo;
    @FXML private HBox scoreOptions;
    @FXML private HBox riskOptions;
    @FXML private Label resultLabel;

    @FXML
    public void initialize() {
        exportTypeCombo.setItems(FXCollections.observableArrayList(Arrays.asList("成绩表", "评语汇总", "高风险学生")));
        exportTypeCombo.getSelectionModel().selectFirst();
        exportTypeCombo.setOnAction(e -> updateOptionsVisibility());

        riskLevelCombo.setItems(FXCollections.observableArrayList(Arrays.asList("HIGH", "MEDIUM", "LOW")));
        riskLevelCombo.getSelectionModel().selectFirst();

        loadClasses();
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
        new Thread(task).start();
    }

    private void loadExams() {
        Task<List<Exam>> task = new Task<>() {
            @Override protected List<Exam> call() throws Exception { return ExamService.getPage(1, 100, null).getRecords(); }
        };
        task.setOnSucceeded(e -> examCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        new Thread(task).start();
    }

    @FXML
    private void handleExport() {
        String type = exportTypeCombo.getValue();
        switch (type) {
            case "成绩表" -> exportScores();
            case "评语汇总" -> exportComments();
            case "高风险学生" -> exportRisks();
        }
    }

    private void exportScores() {
        ClassInfo cls = classCombo.getValue();
        Exam exam = examCombo.getValue();
        if (cls == null || exam == null) {
            CrudHelper.showAlert("请选择班级和考试");
            return;
        }

        Task<List<Score>> task = new Task<>() {
            @Override protected List<Score> call() throws Exception {
                return ScoreService.getScorePage(1, 1000).getRecords();
            }
        };
        task.setOnSucceeded(e -> {
            try {
                File file = showSaveDialog("成绩表.xlsx");
                if (file == null) return;

                XSSFWorkbook wb = new XSSFWorkbook();
                Sheet sheet = wb.createSheet("成绩表");
                Row header = sheet.createRow(0);
                String[] cols = {"学号", "平时分", "卷面分", "总评成绩", "班级排名", "年级排名"};
                for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

                int rowIdx = 1;
                for (Score s : task.getValue()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(s.getStudentId());
                    row.createCell(1).setCellValue(s.getRegularScore());
                    row.createCell(2).setCellValue(s.getExamScore());
                    row.createCell(3).setCellValue(s.getFinalScore());
                    row.createCell(4).setCellValue(s.getRankClass());
                    row.createCell(5).setCellValue(s.getRankGrade());
                }

                try (FileOutputStream fos = new FileOutputStream(file)) { wb.write(fos); }
                resultLabel.setText("导出成功: " + file.getAbsolutePath());
                resultLabel.setVisible(true);
            } catch (Exception ex) {
                CrudHelper.showError("导出失败: " + ex.getMessage());
            }
        });
        new Thread(task).start();
    }

    private void exportComments() {
        Task<List<AiComment>> task = new Task<>() {
            @Override protected List<AiComment> call() throws Exception {
                return CommentService.getPage(1, 1000).getRecords();
            }
        };
        task.setOnSucceeded(e -> {
            try {
                File file = showSaveDialog("评语汇总.xlsx");
                if (file == null) return;

                XSSFWorkbook wb = new XSSFWorkbook();
                Sheet sheet = wb.createSheet("评语汇总");
                Row header = sheet.createRow(0);
                String[] cols = {"学期", "评语内容", "教师已编辑", "创建时间"};
                for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

                int rowIdx = 1;
                for (AiComment c : task.getValue()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(c.getSemester());
                    row.createCell(1).setCellValue(c.getContent());
                    row.createCell(2).setCellValue(c.isTeacherEdited() ? "是" : "否");
                    row.createCell(3).setCellValue(c.getCreatedAt());
                }

                try (FileOutputStream fos = new FileOutputStream(file)) { wb.write(fos); }
                resultLabel.setText("导出成功: " + file.getAbsolutePath());
                resultLabel.setVisible(true);
            } catch (Exception ex) {
                CrudHelper.showError("导出失败: " + ex.getMessage());
            }
        });
        new Thread(task).start();
    }

    private void exportRisks() {
        String riskLevel = riskLevelCombo.getValue();
        Task<List<RiskWarning>> task = new Task<>() {
            @Override protected List<RiskWarning> call() throws Exception {
                return RiskWarningService.getPage(1, 1000, null, riskLevel, null).getRecords();
            }
        };
        task.setOnSucceeded(e -> {
            try {
                File file = showSaveDialog("高风险学生.xlsx");
                if (file == null) return;

                XSSFWorkbook wb = new XSSFWorkbook();
                Sheet sheet = wb.createSheet("高风险学生");
                Row header = sheet.createRow(0);
                String[] cols = {"学号", "学期", "风险等级", "风险原因", "处理状态", "处理备注"};
                for (int i = 0; i < cols.length; i++) header.createCell(i).setCellValue(cols[i]);

                int rowIdx = 1;
                for (RiskWarning w : task.getValue()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(w.getStudentId());
                    row.createCell(1).setCellValue(w.getSemester());
                    row.createCell(2).setCellValue(w.getRiskLevel());
                    row.createCell(3).setCellValue(w.getRiskReason());
                    row.createCell(4).setCellValue(w.getHandleStatus());
                    row.createCell(5).setCellValue(w.getHandleRemark());
                }

                try (FileOutputStream fos = new FileOutputStream(file)) { wb.write(fos); }
                resultLabel.setText("导出成功: " + file.getAbsolutePath());
                resultLabel.setVisible(true);
            } catch (Exception ex) {
                CrudHelper.showError("导出失败: " + ex.getMessage());
            }
        });
        new Thread(task).start();
    }

    private File showSaveDialog(String defaultName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存报表");
        fileChooser.setInitialFileName(defaultName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
        return fileChooser.showSaveDialog(null);
    }
}
