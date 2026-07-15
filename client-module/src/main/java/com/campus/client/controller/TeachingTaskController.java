package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.TeachingTask;
import com.campus.client.service.TeachingTaskService;
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

public class TeachingTaskController implements Initializable {

    @FXML private TableView<TeachingTask> taskTable;
    @FXML private TableColumn<TeachingTask, Long> colId;
    @FXML private TableColumn<TeachingTask, String> colTeacherName;
    @FXML private TableColumn<TeachingTask, String> colCourseName;
    @FXML private TableColumn<TeachingTask, String> colClassName;
    @FXML private TableColumn<TeachingTask, String> colSemester;
    @FXML private TableColumn<TeachingTask, Void> colActions;
    @FXML private Label totalLabel;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;

    private final TeachingTaskService taskService = new TeachingTaskService();
    private int currentPage = 0;
    private int totalPages = 1;
    private final ObservableList<TeachingTask> taskData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTeacherName.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colActions.setCellFactory(col -> new TableCell<>() {
            {
                Button delBtn = new Button("删除");
                delBtn.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;");
                delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                setGraphic(delBtn);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });
        taskTable.setItems(taskData);
    }

    @FXML
    private void handleAdd() {
        AlertHelper.showInfo("新增任务", "请填写教学任务信息后提交");
    }

    private void handleDelete(TeachingTask task) {
        if (AlertHelper.showConfirm("确认删除", "确定要删除此教学任务吗？")) {
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception { taskService.deleteTask(task.getId()); return null; }
            };
            t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "已删除"); loadData(); });
            new Thread(t).start();
        }
    }

    private void loadData() {
        Task<ApiResult<PageResult<TeachingTask>>> task = new Task<>() {
            @Override protected ApiResult<PageResult<TeachingTask>> call() throws Exception { return taskService.getTasks(currentPage, 15); }
        };
        task.setOnSucceeded(e -> {
            ApiResult<PageResult<TeachingTask>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) {
                taskData.setAll(result.getData().getContent());
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
