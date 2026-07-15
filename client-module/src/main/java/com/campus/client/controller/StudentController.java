package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.Student;
import com.campus.client.service.StudentService;
import com.campus.client.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class StudentController implements Initializable {

    @FXML private ComboBox<Long> classFilter;
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, Long> colId;
    @FXML private TableColumn<Student, String> colStudentNo;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colGender;
    @FXML private TableColumn<Student, String> colClassName;
    @FXML private TableColumn<Student, String> colPhone;
    @FXML private TableColumn<Student, String> colStatus;
    @FXML private TableColumn<Student, Void> colActions;
    @FXML private Label totalLabel;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;

    private final StudentService studentService = new StudentService();
    private int currentPage = 0;
    private int totalPages = 1;
    private final ObservableList<Student> studentData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentNo.setCellValueFactory(new PropertyValueFactory<>("studentNo"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
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
        studentTable.setItems(studentData);
    }

    @FXML
    private void handleSearch() {
        currentPage = 0;
        loadData();
    }

    @FXML
    private void handleAdd() {
        AlertHelper.showInfo("新增学生", "请填写学生信息后提交");
    }

    @FXML
    private void handleBatchImport() {
        AlertHelper.showInfo("批量导入", "请通过后端接口 /api/students/batch 批量导入");
    }

    private void handleEdit(Student student) {
        TextInputDialog dialog = new TextInputDialog(student.getName());
        dialog.setTitle("编辑学生");
        dialog.setContentText("姓名:");
        dialog.showAndWait().ifPresent(name -> {
            student.setName(name);
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception { studentService.updateStudent(student.getId(), student); return null; }
            };
            t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "已更新"); loadData(); });
            new Thread(t).start();
        });
    }

    private void handleToggleStatus(Student student) {
        Integer newStatus = student.getStatus() != null && student.getStatus() == 1 ? 0 : 1;
        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception { studentService.updateStudentStatus(student.getId(), newStatus); return null; }
        };
        t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "状态已更新"); loadData(); });
        new Thread(t).start();
    }

    private void loadData() {
        Long classId = classFilter.getValue();
        Task<ApiResult<PageResult<Student>>> task = new Task<>() {
            @Override protected ApiResult<PageResult<Student>> call() throws Exception {
                return studentService.getStudents(currentPage, 15, classId);
            }
        };
        task.setOnSucceeded(e -> {
            ApiResult<PageResult<Student>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) {
                studentData.setAll(result.getData().getContent());
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
