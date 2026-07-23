package com.campus.client.controller;

import com.campus.client.model.AiComment;
import com.campus.client.model.PageResult;
import com.campus.client.service.CommentService;
import com.campus.client.service.SemesterService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    @FXML private ComboBox<String> semesterFilter;
    @FXML private Pagination pagination;
    @FXML private Label emptyLabel;

    private final ObservableList<AiComment> tableData = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

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

        semesterFilter.setOnAction(e -> handleFilterChange());
        loadSemesters();

        pagination.setPageFactory(this::buildPage);
    }

    private void loadSemesters() {
        Task<List<String>> task = new Task<>() {
            @Override protected List<String> call() throws Exception {
                return SemesterService.getAllSemesters();
            }
        };
        task.setOnSucceeded(e -> {
            List<String> semesters = new java.util.ArrayList<>(task.getValue());
            semesters.add(0, "全部");
            semesterFilter.setItems(FXCollections.observableArrayList(semesters));
            EventHandler<ActionEvent> handler = semesterFilter.getOnAction();
            semesterFilter.setOnAction(null);
            semesterFilter.getSelectionModel().selectFirst();
            semesterFilter.setOnAction(handler);
            loadData();
        });
        task.setOnFailed(e -> {
            semesterFilter.setItems(FXCollections.observableArrayList("全部", "2025-1", "2024-2", "2024-1"));
            EventHandler<ActionEvent> handler = semesterFilter.getOnAction();
            semesterFilter.setOnAction(null);
            semesterFilter.getSelectionModel().selectFirst();
            semesterFilter.setOnAction(handler);
            loadData();
        });
        AppExecutors.submit(task::run);
    }

    private void handleFilterChange() {
        currentPage = 1;
        pagination.setCurrentPageIndex(0);
        loadData();
    }

    private Label buildPage(int pageIndex) {
        int targetPage = pageIndex + 1;
        if (targetPage != currentPage) {
            currentPage = targetPage;
            loadData();
        }
        return new Label("");
    }

    private void loadData() {
        String semester = semesterFilter.getValue();
        String semesterParam = "全部".equals(semester) ? null : semester;
        Task<PageResult<AiComment>> task = new Task<>() {
            @Override
            protected PageResult<AiComment> call() throws Exception {
                return CommentService.getMyPage(currentPage, pageSize, semesterParam);
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<AiComment> result = task.getValue();
            List<AiComment> comments = result.getRecords();
            tableData.clear();
            tableData.addAll(comments);
            emptyLabel.setVisible(comments.isEmpty());
            emptyLabel.setManaged(comments.isEmpty());
            table.setVisible(!comments.isEmpty());
            table.setManaged(!comments.isEmpty());
            totalItems = result.getTotal();
            int pageCount = (int) Math.ceil((double) totalItems / pageSize);
            if (pageCount < 1) pageCount = 1;
            pagination.setPageCount(pageCount);
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
