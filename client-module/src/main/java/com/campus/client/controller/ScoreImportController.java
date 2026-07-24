package com.campus.client.controller;

import com.campus.client.model.Course;
import com.campus.client.model.Exam;
import com.campus.client.service.CourseService;
import com.campus.client.service.ExamService;
import com.campus.client.service.ScoreService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import com.campus.client.controller.ImportProgressController;
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
    @FXML private ComboBox<Course> courseCombo;
    @FXML private Label resultLabel;

    @FXML
    public void initialize() {
        loadExams();
        loadCourses();
    }

    private void loadExams() {
        Task<List<Exam>> task = new Task<>() {
            @Override protected List<Exam> call() throws Exception { return ExamService.getPage(1, 100, null).getRecords(); }
        };
        task.setOnSucceeded(e -> examCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> CrudHelper.showError("加载考试列表失败"));
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
        Course course = courseCombo.getValue();
        if (exam == null || course == null) {
            CrudHelper.showAlert("请先选择考试和课程");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择Excel文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件", "*.xlsx", "*.xls"));
        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;

        Task<Long> task = new Task<>() {
            @Override protected Long call() throws Exception {
                return ScoreService.batchImportScore(file, exam.getId(), course.getId());
            }
        };
        task.setOnSucceeded(e -> {
            Long taskId = task.getValue();
            if (taskId != null) {
                resultLabel.setVisible(false);
                ImportProgressController.show(taskId, () -> {});
            } else {
                CrudHelper.showError("导入失败: 未获取到任务ID");
            }
        });
        task.setOnFailed(e -> CrudHelper.showError("导入失败"));
        AppExecutors.submit(task::run);
    }
}
