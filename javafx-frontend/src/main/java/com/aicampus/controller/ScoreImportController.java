package com.aicampus.controller;

import com.aicampus.model.ClassInfo;
import com.aicampus.model.Exam;
import com.aicampus.service.ClassService;
import com.aicampus.service.ExamService;
import com.aicampus.service.ScoreService;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ScoreImportController {

    @FXML private ComboBox<Exam> examCombo;
    @FXML private ComboBox<ClassInfo> classCombo;
    @FXML private Label resultLabel;

    @FXML
    public void initialize() {
        loadExams();
        loadClasses();
    }

    private void loadExams() {
        Task<List<Exam>> task = new Task<>() {
            @Override protected List<Exam> call() throws Exception { return ExamService.getPage(1, 100, null).getRecords(); }
        };
        task.setOnSucceeded(e -> examCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        new Thread(task).start();
    }

    private void loadClasses() {
        Task<List<ClassInfo>> task = new Task<>() {
            @Override protected List<ClassInfo> call() throws Exception { return ClassService.getAll(); }
        };
        task.setOnSucceeded(e -> classCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        new Thread(task).start();
    }

    @FXML
    private void handleDownloadTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("成绩导入模板");
            var header = sheet.createRow(0);
            header.createCell(0).setCellValue("学号");
            header.createCell(1).setCellValue("姓名");
            header.createCell(2).setCellValue("平时分");
            header.createCell(3).setCellValue("卷面分");

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存模板");
            fileChooser.setInitialFileName("成绩导入模板.xlsx");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件", "*.xlsx"));
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                CrudHelper.showAlert("模板已保存到: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            CrudHelper.showError("生成模板失败: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpload() {
        Exam exam = examCombo.getValue();
        ClassInfo classInfo = classCombo.getValue();
        if (exam == null || classInfo == null) {
            CrudHelper.showAlert("请先选择考试和班级");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择Excel文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件", "*.xlsx", "*.xls"));
        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;

        Task<String> task = new Task<>() {
            @Override protected String call() throws Exception {
                return ScoreService.batchImportScore(file, exam.getId(), classInfo.getId());
            }
        };
        task.setOnSucceeded(e -> {
            resultLabel.setText("导入任务已提交，任务ID: " + task.getValue());
            resultLabel.setVisible(true);
        });
        task.setOnFailed(e -> CrudHelper.showError("导入失败"));
        new Thread(task).start();
    }
}
