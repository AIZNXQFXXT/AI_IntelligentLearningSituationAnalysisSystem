package com.aicampus.controller;

import com.aicampus.model.AiComment;
import com.aicampus.service.CommentService;
import com.aicampus.util.AppExecutors;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class StudentCommentController {

    @FXML private TableView<AiComment> table;
    @FXML private TableColumn<AiComment, String> colSemester;
    @FXML private TableColumn<AiComment, String> colContent;
    @FXML private TableColumn<AiComment, Boolean> colEdited;
    @FXML private TableColumn<AiComment, String> colCreatedAt;
    @FXML private Label emptyLabel;

    private final ObservableList<AiComment> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colContent.setCellValueFactory(new PropertyValueFactory<>("content"));
        colEdited.setCellValueFactory(new PropertyValueFactory<>("isTeacherEdited"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colContent.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    label.setWrapText(true);
                    setGraphic(label);
                }
            }
        });

        colEdited.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item ? "教师修改" : "AI生成");
                    label.getStyleClass().add(item ? "tag-warning" : "tag-info");
                    setGraphic(label);
                }
            }
        });

        table.setItems(tableData);
        loadData();
    }

    private void loadData() {
        Task<List<AiComment>> task = new Task<>() {
            @Override
            protected List<AiComment> call() throws Exception {
                return CommentService.getPage(1, 50).getRecords();
            }
        };
        task.setOnSucceeded(e -> {
            List<AiComment> comments = task.getValue();
            tableData.clear();
            tableData.addAll(comments);
            emptyLabel.setVisible(comments.isEmpty());
            emptyLabel.setManaged(comments.isEmpty());
            table.setVisible(!comments.isEmpty());
            table.setManaged(!comments.isEmpty());
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }
}
