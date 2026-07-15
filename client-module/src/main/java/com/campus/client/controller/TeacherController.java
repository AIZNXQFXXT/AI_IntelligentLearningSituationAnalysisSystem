package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.Teacher;
import com.campus.client.service.TeacherService;
import com.campus.client.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class TeacherController implements Initializable {

    @FXML private TableView<Teacher> teacherTable;
    @FXML private TableColumn<Teacher, Long> colId;
    @FXML private TableColumn<Teacher, String> colTeacherNo;
    @FXML private TableColumn<Teacher, String> colName;
    @FXML private TableColumn<Teacher, String> colGender;
    @FXML private TableColumn<Teacher, String> colPhone;
    @FXML private TableColumn<Teacher, String> colEmail;
    @FXML private TableColumn<Teacher, String> colDepartment;
    @FXML private TableColumn<Teacher, String> colStatus;
    @FXML private TableColumn<Teacher, Void> colActions;
    @FXML private Label totalLabel;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;

    private final TeacherService teacherService = new TeacherService();
    private int currentPage = 0;
    private int totalPages = 1;
    private final ObservableList<Teacher> teacherData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTeacherNo.setCellValueFactory(new PropertyValueFactory<>("teacherNo"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus() != null && cellData.getValue().getStatus() == 1 ? "启用" : "禁用"));
        colActions.setCellFactory(col -> new TableCell<>() {
            {
                Button editBtn = new Button("编辑");
                editBtn.setStyle("-fx-background-color: #1E88E5; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;");
                Button statusBtn = new Button("切换状态");
                statusBtn.setStyle("-fx-background-color: #F57F17; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                statusBtn.setOnAction(e -> handleToggleStatus(getTableView().getItems().get(getIndex())));
                setGraphic(new javafx.scene.layout.HBox(4, editBtn, statusBtn));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });
        teacherTable.setItems(teacherData);
    }

    @FXML
    private void handleAdd() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("新增教师");
        dialog.setHeaderText("输入教师工号");
        dialog.setContentText("工号:");
        dialog.showAndWait().ifPresent(no -> {
            Teacher t = new Teacher();
            t.setTeacherNo(no);
            Task<Void> task = new Task<>() {
                @Override protected Void call() throws Exception { teacherService.createTeacher(t); return null; }
            };
            task.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "教师已创建"); loadData(); });
            new Thread(task).start();
        });
    }

    @FXML
    private void handleBatchImport() {
        AlertHelper.showInfo("批量导入", "请通过后端接口 /api/teachers/batch 批量导入");
    }

    private void handleEdit(Teacher teacher) {
        TextInputDialog dialog = new TextInputDialog(teacher.getName());
        dialog.setTitle("编辑教师");
        dialog.setContentText("姓名:");
        dialog.showAndWait().ifPresent(name -> {
            teacher.setName(name);
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception { teacherService.updateTeacher(teacher.getId(), teacher); return null; }
            };
            t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "已更新"); loadData(); });
            new Thread(t).start();
        });
    }

    private void handleToggleStatus(Teacher teacher) {
        Integer newStatus = teacher.getStatus() != null && teacher.getStatus() == 1 ? 0 : 1;
        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception { teacherService.updateTeacherStatus(teacher.getId(), newStatus); return null; }
        };
        t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "状态已更新"); loadData(); });
        new Thread(t).start();
    }

    private void loadData() {
        Task<ApiResult<PageResult<Teacher>>> task = new Task<>() {
            @Override protected ApiResult<PageResult<Teacher>> call() throws Exception { return teacherService.getTeachers(currentPage, 15); }
        };
        task.setOnSucceeded(e -> {
            ApiResult<PageResult<Teacher>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) {
                teacherData.setAll(result.getData().getContent());
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
