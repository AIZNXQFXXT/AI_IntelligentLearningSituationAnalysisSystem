package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.Course;
import com.campus.client.model.PageResult;
import com.campus.client.service.CourseService;
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

public class CourseController implements Initializable {

    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, Long> colId;
    @FXML private TableColumn<Course, String> colCourseName;
    @FXML private TableColumn<Course, String> colCourseCode;
    @FXML private TableColumn<Course, String> colCredit;
    @FXML private TableColumn<Course, String> colTeacherName;
    @FXML private TableColumn<Course, String> colStatus;
    @FXML private TableColumn<Course, Void> colActions;
    @FXML private Label totalLabel;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;

    private final CourseService courseService = new CourseService();
    private int currentPage = 0;
    private int totalPages = 1;
    private final ObservableList<Course> courseData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        colCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));
        colTeacherName.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
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
        courseTable.setItems(courseData);
    }

    @FXML
    private void handleAdd() {
        AlertHelper.showInfo("新增课程", "请填写课程信息后提交");
    }

    private void handleEdit(Course course) {
        TextInputDialog dialog = new TextInputDialog(course.getCourseName());
        dialog.setTitle("编辑课程");
        dialog.setContentText("课程名称:");
        dialog.showAndWait().ifPresent(name -> {
            course.setCourseName(name);
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception { courseService.updateCourse(course.getId(), course); return null; }
            };
            t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "已更新"); loadData(); });
            new Thread(t).start();
        });
    }

    private void handleToggleStatus(Course course) {
        Integer newStatus = course.getStatus() != null && course.getStatus() == 1 ? 0 : 1;
        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception { courseService.updateCourseStatus(course.getId(), newStatus); return null; }
        };
        t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "状态已更新"); loadData(); });
        new Thread(t).start();
    }

    private void loadData() {
        Task<ApiResult<PageResult<Course>>> task = new Task<>() {
            @Override protected ApiResult<PageResult<Course>> call() throws Exception { return courseService.getCourses(currentPage, 15); }
        };
        task.setOnSucceeded(e -> {
            ApiResult<PageResult<Course>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) {
                courseData.setAll(result.getData().getContent());
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
