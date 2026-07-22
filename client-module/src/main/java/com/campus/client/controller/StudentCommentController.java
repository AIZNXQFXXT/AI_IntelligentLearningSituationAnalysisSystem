package com.aicampus.controller;

import com.aicampus.model.AiComment;
import com.aicampus.service.CommentService;
import com.aicampus.util.AppExecutors;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class StudentCommentController {

    @FXML private TableView<AiComment> table;
    @FXML private TableColumn<AiComment, String> colSemester;
    @FXML private TableColumn<AiComment, String> colContent;
    @FXML private TableColumn<AiComment, Boolean> colEdited;
    @FXML private TableColumn<AiComment, String> colCreatedAt;
    @FXML private TableColumn<AiComment, Void> colAction;
    @FXML private Label emptyLabel;

    private final ObservableList<AiComment> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colContent.setCellValueFactory(new PropertyValueFactory<>("content"));
        colEdited.setCellValueFactory(new PropertyValueFactory<>("teacherEdited"));
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

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("查看");
            {
                btn.getStyleClass().addAll("btn-primary", "btn-sm");
                btn.setOnAction(e -> handleView(getTableView().getItems().get(getIndex())));
                HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : btn.getParent());
            }
        });

        table.setItems(tableData);
        loadData();
    }

    private void loadData() {
        Task<List<AiComment>> task = new Task<>() {
            @Override
            protected List<AiComment> call() throws Exception {
                return CommentService.getMyPage(1, 50).getRecords();
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

    private void handleView(AiComment c) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("评语详情");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().addAll(
            new Label("学期: " + c.getSemester()),
            new Label(c.getContent())
        );
        dialog.getDialogPane().setContent(vbox);
        dialog.showAndWait();
    }
}
