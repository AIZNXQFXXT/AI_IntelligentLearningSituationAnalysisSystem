package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.ClassInfo;
import com.campus.client.model.PageResult;
import com.campus.client.service.ClassService;
import com.campus.client.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClassController implements Initializable {

    @FXML private TableView<ClassInfo> classTable;
    @FXML private TableColumn<ClassInfo, Long> colId;
    @FXML private TableColumn<ClassInfo, String> colClassName;
    @FXML private TableColumn<ClassInfo, String> colGrade;
    @FXML private TableColumn<ClassInfo, String> colTeacherName;
    @FXML private TableColumn<ClassInfo, Integer> colStudentCount;
    @FXML private TableColumn<ClassInfo, String> colCreateTime;
    @FXML private TableColumn<ClassInfo, Void> colActions;
    @FXML private Label totalLabel;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;

    private final ClassService classService = new ClassService();
    private int currentPage = 0;
    private int totalPages = 1;
    private final ObservableList<ClassInfo> classData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("grade"));
        colTeacherName.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        colStudentCount.setCellValueFactory(new PropertyValueFactory<>("studentCount"));
        colCreateTime.setCellValueFactory(new PropertyValueFactory<>("createTime"));
        colActions.setCellFactory(col -> new TableCell<>() {
            {
                Button editBtn = new Button("编辑");
                editBtn.setStyle("-fx-background-color: #1E88E5; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;");
                Button delBtn = new Button("删除");
                delBtn.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                setGraphic(new javafx.scene.layout.HBox(4, editBtn, delBtn));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });
        classTable.setItems(classData);
    }

    @FXML
    private void handleAdd() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("新增班级");
        dialog.setHeaderText("请输入班级信息");
        dialog.setContentText("班级名称:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            ClassInfo ci = new ClassInfo();
            ci.setClassName(name);
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception {
                    classService.createClass(ci);
                    return null;
                }
            };
            t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "班级已创建"); loadData(); });
            new Thread(t).start();
        });
    }

    private void handleEdit(ClassInfo classInfo) {
        TextInputDialog dialog = new TextInputDialog(classInfo.getClassName());
        dialog.setTitle("编辑班级");
        dialog.setContentText("班级名称:");
        dialog.showAndWait().ifPresent(name -> {
            classInfo.setClassName(name);
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception {
                    classService.updateClass(classInfo.getId(), classInfo);
                    return null;
                }
            };
            t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "已更新"); loadData(); });
            new Thread(t).start();
        });
    }

    private void handleDelete(ClassInfo classInfo) {
        if (AlertHelper.showConfirm("确认删除", "确定要删除班级 " + classInfo.getClassName() + " 吗？")) {
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception {
                    classService.deleteClass(classInfo.getId());
                    return null;
                }
            };
            t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "已删除"); loadData(); });
            new Thread(t).start();
        }
    }

    @FXML
    private void handleExport() {
        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception {
                byte[] data = classService.exportExcel();
                File file = new File("class_export.xlsx");
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(data);
                }
                return null;
            }
        };
        t.setOnSucceeded(e -> AlertHelper.showInfo("成功", "导出成功"));
        t.setOnFailed(e -> AlertHelper.showError("失败", "导出失败"));
        new Thread(t).start();
    }

    private void loadData() {
        Task<ApiResult<PageResult<ClassInfo>>> task = new Task<>() {
            @Override protected ApiResult<PageResult<ClassInfo>> call() throws Exception {
                return classService.getClasses(currentPage, 15);
            }
        };
        task.setOnSucceeded(e -> {
            ApiResult<PageResult<ClassInfo>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) {
                classData.setAll(result.getData().getContent());
                totalPages = Math.max(1, result.getData().getTotalPages());
                pageInfo.setText("第 " + (currentPage + 1) + " 页 / 共 " + totalPages + " 页");
                totalLabel.setText("共 " + result.getData().getTotalElements() + " 条");
            }
        });
        new Thread(task).start();
    }

    @FXML private void handlePrevPage() { if (currentPage > 0) { currentPage--; loadData(); } }
    @FXML private void handleNextPage() { if (currentPage < totalPages - 1) { currentPage++; loadData(); } }
}
