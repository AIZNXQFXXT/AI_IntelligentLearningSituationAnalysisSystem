package com.aicampus.controller;

import com.aicampus.model.ClassInfo;
import com.aicampus.model.PageResult;
import com.aicampus.model.Teacher;
import com.aicampus.service.ClassService;
import com.aicampus.service.TeacherService;
import com.aicampus.util.AppExecutors;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import com.aicampus.util.TableUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassManagementController {

    @FXML private TextField keywordField;
    @FXML private TableView<ClassInfo> table;
    @FXML private TableColumn<ClassInfo, String> colGrade;
    @FXML private TableColumn<ClassInfo, String> colClassName;
    @FXML private TableColumn<ClassInfo, String> colHeadTeacherId;
    @FXML private TableColumn<ClassInfo, Integer> colStudentCount;
    @FXML private TableColumn<ClassInfo, String> colCreatedAt;
    @FXML private TableColumn<ClassInfo, Void> colActions;
    @FXML private Pagination pagination;

    private final ObservableList<ClassInfo> tableData = FXCollections.observableArrayList();
    private final Map<Integer, String> teacherMap = new HashMap<>();
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

    @FXML
    public void initialize() {
        colGrade.setCellValueFactory(new PropertyValueFactory<>("grade"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colHeadTeacherId.setCellValueFactory(data ->
                new SimpleStringProperty(teacherMap.getOrDefault(data.getValue().getHeadTeacherId(), "未知")));
        colStudentCount.setCellValueFactory(new PropertyValueFactory<>("studentCount"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        TableUtils.setupActionColumn(colActions, this::handleEdit, this::handleDelete);

        table.setItems(tableData);
        loadTeacherMap();
        loadData();
    }

    private void loadTeacherMap() {
        Task<List<Teacher>> task = new Task<>() {
            @Override
            protected List<Teacher> call() throws Exception {
                return TeacherService.getAll();
            }
        };
        task.setOnSucceeded(e -> {
            teacherMap.clear();
            for (Teacher t : task.getValue()) {
                teacherMap.put(t.getId(), t.getName() + " (" + t.getTeacherNo() + ")");
            }
            table.refresh();
        });
        task.setOnFailed(e -> {});
        AppExecutors.submit(task::run);
    }

    private void loadData() {
        String keyword = keywordField.getText();
        Task<PageResult<ClassInfo>> task = new Task<>() {
            @Override
            protected PageResult<ClassInfo> call() throws Exception {
                return ClassService.getPage(currentPage, pageSize, keyword);
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<ClassInfo> result = task.getValue();
            tableData.clear();
            tableData.addAll(result.getRecords());
            totalItems = result.getTotal();
            updatePagination();
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) totalItems / pageSize);
        if (pageCount < 1) pageCount = 1;
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(currentPage - 1);
        pagination.setPageFactory(pageIndex -> {
            if (pageIndex + 1 != currentPage) {
                currentPage = pageIndex + 1;
                loadData();
            }
            return new Label("");
        });
    }

    @FXML
    private void onSearchKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            currentPage = 1;
            loadData();
        }
    }

    @FXML
    private void handleCreate() {
        showFormDialog("新增班级", null);
    }

    @FXML
    private void handleBatchImport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择Excel文件");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件", "*.xlsx", "*.xls"));
        File file = chooser.showOpenDialog(table.getScene().getWindow());
        if (file != null) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception { ClassService.batchImport(file); return null; }
            };
            task.setOnSucceeded(e -> { CrudHelper.showAlert("导入成功"); loadData(); });
            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "导入失败");
            });
            AppExecutors.submit(task::run);
        }
    }

    private void handleEdit(ClassInfo item) {
        showFormDialog("编辑班级", item);
    }

    private void showFormDialog(String title, ClassInfo existing) {
        Dialog<ClassInfo> dialog = new Dialog<>();
        dialog.setTitle(title);

        TextField gradeField = new TextField();
        gradeField.setPromptText("如: 2024级");
        if (existing != null) gradeField.setText(existing.getGrade());

        TextField classNameField = new TextField();
        classNameField.setPromptText("如: 计算机1班");
        if (existing != null) classNameField.setText(existing.getClassName());

        ComboBox<String> headTeacherBox = new ComboBox<>();
        headTeacherBox.setPromptText("选择班主任");
        Task<List<Teacher>> loadTeachers = new Task<>() {
            @Override protected List<Teacher> call() throws Exception { return TeacherService.getAll(); }
        };
        loadTeachers.setOnSucceeded(ev -> {
            for (Teacher t : loadTeachers.getValue()) {
                headTeacherBox.getItems().add(t.getId() + " - " + t.getTeacherNo() + " - " + t.getName());
            }
            if (existing != null && existing.getHeadTeacherId() > 0) {
                for (String item : headTeacherBox.getItems()) {
                    if (item.startsWith(existing.getHeadTeacherId() + " - ")) {
                        headTeacherBox.setValue(item);
                        break;
                    }
                }
            }
        });
        AppExecutors.submit(loadTeachers::run);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.add(new Label("年级:"), 0, 0);
        grid.add(gradeField, 1, 0);
        grid.add(new Label("班级名:"), 0, 1);
        grid.add(classNameField, 1, 1);
        grid.add(new Label("班主任:"), 0, 2);
        grid.add(headTeacherBox, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (gradeField.getText().isEmpty() || classNameField.getText().isEmpty()) {
                    CrudHelper.showError("请填写必填项");
                    return null;
                }
                ClassInfo info = existing != null ? existing : new ClassInfo();
                if (existing != null) info.setId(existing.getId());
                info.setGrade(gradeField.getText());
                info.setClassName(classNameField.getText());
                String headId = headTeacherBox.getValue();
                if (headId != null) info.setHeadTeacherId(Integer.parseInt(headId.split(" - ")[0]));
                return info;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            Task<Void> saveTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    if (existing != null) {
                        ClassService.update(existing.getId(), result);
                    } else {
                        ClassService.create(result);
                    }
                    return null;
                }
            };
            saveTask.setOnSucceeded(e -> {
                CrudHelper.showAlert(existing != null ? "更新成功" : "新增成功");
                loadData();
            });
            saveTask.setOnFailed(e -> {
                Throwable ex = saveTask.getException();
                CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "操作失败");
            });
            AppExecutors.submit(saveTask::run);
        });
    }

    private void handleDelete(ClassInfo item) {
        CrudHelper.confirmDelete(selected -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    ClassService.delete(selected.getId());
                    return null;
                }
            };
            task.setOnSucceeded(e -> {
                CrudHelper.showAlert("删除成功");
                loadData();
            });
            task.setOnFailed(e -> CrudHelper.showError("删除失败"));
            AppExecutors.submit(task::run);
        }, item);
    }
}
