package com.aicampus.controller;

import com.aicampus.model.AiComment;
import com.aicampus.service.CommentService;
import com.aicampus.util.AppExecutors;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.Arrays;
import java.util.List;

public class TeacherCommentController {

    @FXML private TableView<AiComment> table;
    @FXML private TableColumn<AiComment, Integer> colId;
    @FXML private TableColumn<AiComment, String> colSemester;
    @FXML private TableColumn<AiComment, String> colContent;
    @FXML private TableColumn<AiComment, Boolean> colEdited;
    @FXML private TableColumn<AiComment, String> colCreatedAt;
    @FXML private TableColumn<AiComment, Void> colAction;
    @FXML private ComboBox<String> semesterCombo;

    private final ObservableList<AiComment> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        semesterCombo.setItems(FXCollections.observableArrayList(Arrays.asList("2025-1", "2024-2", "2024-1")));
        semesterCombo.getSelectionModel().selectFirst();
        semesterCombo.setOnAction(e -> loadData());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colContent.setCellValueFactory(new PropertyValueFactory<>("content"));
        colEdited.setCellValueFactory(new PropertyValueFactory<>("isTeacherEdited"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colContent.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label label = new Label(item); label.setWrapText(true); setGraphic(label);
            }
        });

        colEdited.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label label = new Label(item ? "是" : "否");
                label.getStyleClass().add(item ? "tag-warning" : "tag-info");
                setGraphic(label);
            }
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button versionBtn = new Button("版本");
            {
                editBtn.getStyleClass().addAll("btn-primary", "btn-sm");
                versionBtn.getStyleClass().addAll("btn-info", "btn-sm");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                versionBtn.setOnAction(e -> showVersions(getTableView().getItems().get(getIndex())));
                HBox box = new HBox(5, editBtn, versionBtn); box.setAlignment(Pos.CENTER); setGraphic(box);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn.getParent());
            }
        });

        table.setItems(tableData);
        loadData();
    }

    private void loadData() {
        String semester = semesterCombo.getValue();
        Task<List<AiComment>> task = new Task<>() {
            @Override protected List<AiComment> call() throws Exception {
                List<AiComment> all = CommentService.getPage(1, 200).getRecords();
                if (semester != null) {
                    all.removeIf(c -> !semester.equals(c.getSemester()));
                }
                return all;
            }
        };
        task.setOnSucceeded(e -> { tableData.clear(); tableData.addAll(task.getValue()); });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }

    @FXML
    private void handleBatchGenerate() {
        String semester = semesterCombo.getValue();
        if (semester == null) {
            CrudHelper.showError("请先选择学期");
            return;
        }
        CrudHelper.showAlert("批量生成已启动，学期: " + semester);
    }

    private void handleEdit(AiComment c) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("编辑评语");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextArea textArea = new TextArea(c.getContent());
        textArea.setPrefRowCount(6);
        textArea.setWrapText(true);
        dialog.getDialogPane().setContent(textArea);

        dialog.setResultConverter(bt -> bt == ButtonType.OK ? textArea.getText() : null);
        dialog.showAndWait().ifPresent(content -> {
            c.setContent(content);
            c.setTeacherEdited(true);
            table.refresh();
            CrudHelper.showAlert("保存成功");
        });
    }

    private void showVersions(AiComment c) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("版本历史");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10);
        vbox.setPadding(new javafx.geometry.Insets(20));

        Label current = new Label(String.format("当前版本 - %s\n%s", c.getCreatedAt(), c.getContent()));
        current.setWrapText(true);
        vbox.getChildren().add(current);

        dialog.getDialogPane().setContent(vbox);
        dialog.showAndWait();
    }
}
