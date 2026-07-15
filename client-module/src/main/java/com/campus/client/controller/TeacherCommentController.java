package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.Comment;
import com.campus.client.model.PageResult;
import com.campus.client.service.CommentService;
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

public class TeacherCommentController implements Initializable {

    @FXML private TableView<Comment> commentTable;
    @FXML private TableColumn<Comment, Long> colId;
    @FXML private TableColumn<Comment, String> colStudentName;
    @FXML private TableColumn<Comment, String> colCourseName;
    @FXML private TableColumn<Comment, String> colContent;
    @FXML private TableColumn<Comment, String> colType;
    @FXML private TableColumn<Comment, String> colCreateTime;
    @FXML private TableColumn<Comment, Void> colActions;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;

    private final CommentService commentService = new CommentService();
    private int currentPage = 0;
    private int totalPages = 1;
    private final ObservableList<Comment> commentData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colContent.setCellValueFactory(new PropertyValueFactory<>("content"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colCreateTime.setCellValueFactory(new PropertyValueFactory<>("createTime"));
        colActions.setCellFactory(col -> new TableCell<>() {
            {
                Button editBtn = new Button("编辑");
                editBtn.setStyle("-fx-background-color: #1E88E5; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                setGraphic(editBtn);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });
        commentTable.setItems(commentData);
    }

    @FXML
    private void handleAdd() {
        AlertHelper.showInfo("新增评语", "请填写评语信息后提交");
    }

    private void handleEdit(Comment comment) {
        TextInputDialog dialog = new TextInputDialog(comment.getContent());
        dialog.setTitle("编辑评语");
        dialog.setContentText("评语内容:");
        dialog.showAndWait().ifPresent(content -> {
            comment.setContent(content);
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception { commentService.updateComment(comment.getId(), comment); return null; }
            };
            t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "已更新"); loadData(); });
            new Thread(t).start();
        });
    }

    private void loadData() {
        Task<ApiResult<PageResult<Comment>>> task = new Task<>() {
            @Override protected ApiResult<PageResult<Comment>> call() throws Exception { return commentService.getComments(currentPage, 15, null); }
        };
        task.setOnSucceeded(e -> {
            ApiResult<PageResult<Comment>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) {
                commentData.setAll(result.getData().getContent());
                totalPages = Math.max(1, result.getData().getTotalPages());
                pageInfo.setText("第 " + (currentPage + 1) + " 页 / 共 " + totalPages + " 页");
            }
        });
        new Thread(task).start();
    }

    @FXML private void handlePrevPage() { if (currentPage > 0) { currentPage--; loadData(); } }
    @FXML private void handleNextPage() { if (currentPage < totalPages - 1) { currentPage++; loadData(); } }
}
