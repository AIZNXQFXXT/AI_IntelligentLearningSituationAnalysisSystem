package com.campus.client.controller;

import com.campus.client.model.ClassInfo;
import com.campus.client.model.PageResult;
import com.campus.client.model.Student;
import com.campus.client.service.ClassService;
import com.campus.client.service.StudentService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.application.Platform;

import java.io.File;
import java.util.List;

public class StudentManagementController {

    @FXML private ComboBox<String> classFilter;
    @FXML private TextField keywordField;
    @FXML private TableView<Student> table;
    @FXML private TableColumn<Student, String> colStudentNo;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colGender;
    @FXML private TableColumn<Student, String> colClassName;
    @FXML private TableColumn<Student, Integer> colEnrollYear;
    @FXML private TableColumn<Student, String> colPhone;
    @FXML private TableColumn<Student, String> colStatus;
    @FXML private TableColumn<Student, Void> colActions;
    @FXML private Pagination pagination;

    private final ObservableList<Student> tableData = FXCollections.observableArrayList();
    private final ObservableList<String> classNames = FXCollections.observableArrayList();
    private List<ClassInfo> classList;
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

    @FXML
    public void initialize() {
        colStudentNo.setCellValueFactory(new PropertyValueFactory<>("studentNo"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colEnrollYear.setCellValueFactory(new PropertyValueFactory<>("enrollYear"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colStatus.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus() == 1 ? "正常" : "停用"));

        TableUtils.setupActionColumn(colActions, this::handleEdit, this::handleDelete, this::handleStatusToggle,
            student -> student.getStatus() == 1 ? "停用" : "启用");

        classFilter.setItems(classNames);
        classFilter.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            currentPage = 1;
            pagination.setCurrentPageIndex(0);
            loadData();
        });

        table.setItems(tableData);
        // 分页工厂只注册一次：用户点页码时由它触发加载
        pagination.setPageFactory(this::buildPage);

        loadClasses();
    }

    private Label buildPage(int pageIndex) {
        int targetPage = pageIndex + 1;
        if (targetPage != currentPage) {
            currentPage = targetPage;
            loadData();
        }
        return new Label("");
    }

    private void loadClasses() {
        Task<List<ClassInfo>> task = new Task<>() {
            @Override
            protected List<ClassInfo> call() throws Exception { return ClassService.getAll(); }
        };
        task.setOnSucceeded(e -> {
            classList = task.getValue();
            classNames.clear();
            classNames.add("全部班级");
            for (ClassInfo c : classList) classNames.add(c.getClassName());
            applyClassNameMapping();
            loadData();
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载班级列表失败");
        });
        AppExecutors.submit(task::run);
    }

    private void applyClassNameMapping() {
        if (classList == null) return;
        for (Student s : tableData) {
            if (s.getClassName() == null && s.getClassId() != 0) {
                for (ClassInfo c : classList) {
                    if (c.getId() == s.getClassId()) {
                        s.setClassName(c.getClassName());
                        break;
                    }
                }
            }
        }
        table.refresh();
    }

    private Integer getSelectedClassId() {
        int idx = classFilter.getSelectionModel().getSelectedIndex();
        if (idx <= 0 || classList == null || idx > classList.size()) return null;
        return classList.get(idx - 1).getId();
    }

    private void loadData() {
        String keyword = keywordField.getText();
        Integer classId = getSelectedClassId();
        Task<PageResult<Student>> task = new Task<>() {
            @Override
            protected PageResult<Student> call() throws Exception {
                return StudentService.getPage(currentPage, pageSize, keyword, classId);
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<Student> result = task.getValue();
            tableData.clear();
            tableData.addAll(result.getRecords());
            applyClassNameMapping();
            totalItems = result.getTotal();
            int pageCount = (int) Math.ceil((double) totalItems / pageSize);
            if (pageCount < 1) pageCount = 1;
            pagination.setPageCount(pageCount);
            // 此处不再调用 setCurrentPageIndex / setPageFactory，否则异步响应会把指针拽回本次页，触发连环重载
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }

    @FXML
    private void onSearchKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            currentPage = 1;
            pagination.setCurrentPageIndex(0);
            loadData();
        }
    }

    @FXML
    private void handleCreate() { showFormDialog("新增学生", null); }

    private void handleEdit(Student item) { showFormDialog("编辑学生", item); }

    private void showFormDialog(String title, Student existing) {
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle(title);

        TextField studentNoField = new TextField();
        TextField nameField = new TextField();
        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("男", "女");
        ComboBox<String> classBox = new ComboBox<>();
        if (classList != null) {
            for (ClassInfo c : classList) classBox.getItems().add(c.getId() + " - " + c.getClassName());
        }
        Spinner<Integer> enrollYearSpinner = new Spinner<>(2000, 2099, java.time.Year.now().getValue());
        enrollYearSpinner.setEditable(true);
        enrollYearSpinner.setPrefWidth(150);
        TextField phoneField = new TextField();
        TextField guardianPhoneField = new TextField();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        if (existing != null) {
            studentNoField.setText(existing.getStudentNo());
            nameField.setText(existing.getName());
            genderBox.setValue(existing.getGender());
            enrollYearSpinner.getValueFactory().setValue(existing.getEnrollYear());
            phoneField.setText(existing.getPhone());
            guardianPhoneField.setText(existing.getGuardianPhone());
            studentNoField.setDisable(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        int r = 0;
        grid.add(new Label("学号:"), 0, r); grid.add(studentNoField, 1, r++);
        grid.add(new Label("姓名:"), 0, r); grid.add(nameField, 1, r++);
        grid.add(new Label("性别:"), 0, r); grid.add(genderBox, 1, r++);
        grid.add(new Label("班级:"), 0, r); grid.add(classBox, 1, r++);
        grid.add(new Label("入学年份:"), 0, r); grid.add(enrollYearSpinner, 1, r++);
        grid.add(new Label("电话:"), 0, r); grid.add(phoneField, 1, r++);
        grid.add(new Label("监护人电话:"), 0, r); grid.add(guardianPhoneField, 1, r++);
        if (existing == null) {
            grid.add(new Label("用户名:"), 0, r); grid.add(usernameField, 1, r++);
            grid.add(new Label("密码:"), 0, r); grid.add(passwordField, 1, r++);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (studentNoField.getText().isEmpty() || nameField.getText().isEmpty()) {
                    CrudHelper.showError("请填写必填项"); return null;
                }
                Student s = existing != null ? existing : new Student();
                if (existing == null) {
                    s.setStatus(1);
                }
                s.setStudentNo(studentNoField.getText());
                s.setName(nameField.getText());
                s.setGender(genderBox.getValue());
                String cv = classBox.getValue();
                if (cv != null) s.setClassId(Integer.parseInt(cv.split(" - ")[0]));
                s.setEnrollYear(enrollYearSpinner.getValue());
                s.setPhone(phoneField.getText());
                s.setGuardianPhone(guardianPhoneField.getText());
                if (existing == null) {
                    String uname = usernameField.getText().trim();
                    String pwd = passwordField.getText().trim();
                    if (!uname.isEmpty()) s.setUsername(uname);
                    if (!pwd.isEmpty()) s.setPassword(pwd);
                }
                return s;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            Task<Void> saveTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    if (existing != null) StudentService.update(existing.getId(), result);
                    else StudentService.create(result);
                    return null;
                }
            };
            saveTask.setOnSucceeded(e -> { CrudHelper.showAlert(existing != null ? "更新成功" : "新增成功"); loadData(); });
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
                protected Void call() throws Exception { StudentService.batchImport(file); return null; }
            };
            task.setOnSucceeded(e -> { CrudHelper.showAlert("导入成功"); loadData(); });
            task.setOnFailed(e -> {
                Throwable ex = task.getException();
                CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "导入失败");
            });
            AppExecutors.submit(task::run);
        }
    }

    private void handleDelete(Student item) {
        CrudHelper.confirmDelete(selected -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception { StudentService.delete(selected.getId()); return null; }
            };
            task.setOnSucceeded(e -> { CrudHelper.showAlert("删除成功"); loadData(); });
            task.setOnFailed(e -> CrudHelper.showError("删除失败"));
            AppExecutors.submit(task::run);
        }, item);
    }

    private void handleStatusToggle(Student item) {
        int newStatus = item.getStatus() == 1 ? 0 : 1;
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                return StudentService.toggleStatus(item.getId(), newStatus);
            }
        };
        task.setOnSucceeded(e -> {
            CrudHelper.showAlert("状态已更新");
            loadData();
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "状态更新失败");
        });
        AppExecutors.submit(task::run);
    }
}
