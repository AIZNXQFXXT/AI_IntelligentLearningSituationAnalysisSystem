package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.Exam;
import com.campus.client.model.PageResult;
import com.campus.client.service.ExamService;
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

public class ExamController implements Initializable {

    @FXML private TableView<Exam> examTable;
    @FXML private TableColumn<Exam, Long> colId;
    @FXML private TableColumn<Exam, String> colExamName;
    @FXML private TableColumn<Exam, String> colCourseName;
    @FXML private TableColumn<Exam, String> colExamType;
    @FXML private TableColumn<Exam, String> colExamDate;
    @FXML private TableColumn<Exam, String> colStartTime;
    @FXML private TableColumn<Exam, String> colEndTime;
    @FXML private TableColumn<Exam, Void> colActions;
    @FXML private Label totalLabel;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;

    private final ExamService examService = new ExamService();
    private int currentPage = 0;
    private int totalPages = 1;
    private final ObservableList<Exam> examData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colExamName.setCellValueFactory(new PropertyValueFactory<>("examName"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colExamType.setCellValueFactory(new PropertyValueFactory<>("examType"));
        colExamDate.setCellValueFactory(new PropertyValueFactory<>("examDate"));
        colStartTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
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
        examTable.setItems(examData);
    }

    @FXML
    private void handleAdd() {
        AlertHelper.showInfo("新增考试", "请填写考试批次信息后提交");
    }

    private void handleEdit(Exam exam) {
        TextInputDialog dialog = new TextInputDialog(exam.getExamName());
        dialog.setTitle("编辑考试");
        dialog.setContentText("考试名称:");
        dialog.showAndWait().ifPresent(name -> {
            exam.setExamName(name);
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception { examService.updateExam(exam.getId(), exam); return null; }
            };
            t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "已更新"); loadData(); });
            new Thread(t).start();
        });
    }

    private void handleDelete(Exam exam) {
        if (AlertHelper.showConfirm("确认删除", "确定要删除考试 " + exam.getExamName() + " 吗？")) {
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception { examService.deleteExam(exam.getId()); return null; }
            };
            t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "已删除"); loadData(); });
            new Thread(t).start();
        }
    }

    private void loadData() {
        Task<ApiResult<PageResult<Exam>>> task = new Task<>() {
            @Override protected ApiResult<PageResult<Exam>> call() throws Exception { return examService.getExams(currentPage, 15); }
        };
        task.setOnSucceeded(e -> {
            ApiResult<PageResult<Exam>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) {
                examData.setAll(result.getData().getContent());
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
