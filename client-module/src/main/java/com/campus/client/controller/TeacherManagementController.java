package com.campus.client.controller;

import com.campus.client.model.PageResult;
import com.campus.client.model.Teacher;
import com.campus.client.service.TeacherService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import com.campus.client.util.TableUtils;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.Year;

public class TeacherManagementController {

    @FXML private TextField keywordField;
    @FXML private TableView<Teacher> table;
    @FXML private TableColumn<Teacher, String> colTeacherNo;
    @FXML private TableColumn<Teacher, String> colName;
    @FXML private TableColumn<Teacher, String> colTitle;
    @FXML private TableColumn<Teacher, String> colSubject;
    @FXML private TableColumn<Teacher, String> colDepartment;
    @FXML private TableColumn<Teacher, String> colEducation;
    @FXML private TableColumn<Teacher, Void> colActions;
    @FXML private Pagination pagination;

    private final ObservableList<Teacher> tableData = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

    @FXML
    public void initialize() {
        colTeacherNo.setCellValueFactory(new PropertyValueFactory<>("teacherNo"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colEducation.setCellValueFactory(new PropertyValueFactory<>("education"));

        TableUtils.setupActionColumn(colActions, this::handleEdit, this::handleDelete);

        table.setItems(tableData);
        loadData();
    }

    private void loadData() {
        String keyword = keywordField.getText();
        Task<PageResult<Teacher>> task = new Task<>() {
            @Override
            protected PageResult<Teacher> call() throws Exception {
                return TeacherService.getPage(currentPage, pageSize, keyword);
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<Teacher> result = task.getValue();
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
    private void handleCreate() { showFormDialog("新增教师", null); }

    private void handleEdit(Teacher item) { showFormDialog("编辑教师", item); }

    private void showFormDialog(String title, Teacher existing) {
        Dialog<Teacher> dialog = new Dialog<>();
        dialog.setTitle(title);

        TextField teacherNoField = new TextField();
        TextField nameField = new TextField();
        TextField titleField = new TextField();
        TextField subjectField = new TextField();
        TextField educationField = new TextField();
        TextField departmentField = new TextField();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        ComboBox<String> semesterCombo = new ComboBox<>();
        int year = Year.now().getValue();
        semesterCombo.setItems(FXCollections.observableArrayList(year + "-1", year + "-2"));
        semesterCombo.getSelectionModel().selectFirst();

        if (existing != null) {
            teacherNoField.setText(existing.getTeacherNo());
            nameField.setText(existing.getName());
            titleField.setText(existing.getTitle());
            subjectField.setText(existing.getSubject());
            educationField.setText(existing.getEducation());
            departmentField.setText(existing.getDepartment());
            if (existing.getSemester() != null) semesterCombo.setValue(existing.getSemester());
            teacherNoField.setDisable(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        int row = 0;
        grid.add(new Label("工号:"), 0, row); grid.add(teacherNoField, 1, row++);
        grid.add(new Label("姓名:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("职称:"), 0, row); grid.add(titleField, 1, row++);
        grid.add(new Label("学科:"), 0, row); grid.add(subjectField, 1, row++);
        grid.add(new Label("学历:"), 0, row); grid.add(educationField, 1, row++);
        grid.add(new Label("部门:"), 0, row); grid.add(departmentField, 1, row++);
        grid.add(new Label("学期:"), 0, row); grid.add(semesterCombo, 1, row++);
        if (existing == null) {
            grid.add(new Label("用户名:"), 0, row); grid.add(usernameField, 1, row++);
            grid.add(new Label("密码:"), 0, row); grid.add(passwordField, 1, row++);
        }

        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (teacherNoField.getText().isEmpty() || nameField.getText().isEmpty()) {
                    CrudHelper.showError("请填写必填项");
                    return null;
                }
                if (existing == null && (usernameField.getText().isEmpty() || passwordField.getText().isEmpty())) {
                    CrudHelper.showError("请填写用户名和密码");
                    return null;
                }
                Teacher t = existing != null ? existing : new Teacher();
                t.setTeacherNo(teacherNoField.getText());
                t.setName(nameField.getText());
                t.setTitle(titleField.getText());
                t.setSubject(subjectField.getText());
                t.setEducation(educationField.getText());
                t.setDepartment(departmentField.getText());
                t.setSemester(semesterCombo.getValue());
                return t;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            Task<Void> saveTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    if (existing != null) TeacherService.update(existing.getId(), result);
                    else TeacherService.create(result);
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

    @FXML
    private void handleBatchImport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择Excel文件");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel文件", "*.xlsx", "*.xls"));
        File file = chooser.showOpenDialog(table.getScene().getWindow());
        if (file != null) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    TeacherService.batchImport(file);
                    return null;
                }
            };
            task.setOnSucceeded(e -> { CrudHelper.showAlert("导入成功"); loadData(); });
            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "导入失败");
            });
            AppExecutors.submit(task::run);
        }
    }

    private void handleDelete(Teacher item) {
        CrudHelper.confirmDelete(selected -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception { TeacherService.delete(selected.getId()); return null; }
            };
            task.setOnSucceeded(e -> { CrudHelper.showAlert("删除成功"); loadData(); });
            task.setOnFailed(e -> CrudHelper.showError("删除失败"));
            AppExecutors.submit(task::run);
        }, item);
    }
}
