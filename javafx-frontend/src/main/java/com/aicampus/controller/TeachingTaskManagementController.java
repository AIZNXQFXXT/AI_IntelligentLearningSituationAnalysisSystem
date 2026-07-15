package com.aicampus.controller;

import com.aicampus.model.PageResult;
import com.aicampus.model.TeachingTask;
import com.aicampus.service.TeachingTaskService;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class TeachingTaskManagementController {

    @FXML private TextField semesterField;
    @FXML private TableView<TeachingTask> table;
    @FXML private TableColumn<TeachingTask, Integer> colId;
    @FXML private TableColumn<TeachingTask, Integer> colTeacherId;
    @FXML private TableColumn<TeachingTask, Integer> colClassId;
    @FXML private TableColumn<TeachingTask, Integer> colCourseId;
    @FXML private TableColumn<TeachingTask, String> colSemester;
    @FXML private TableColumn<TeachingTask, String> colCreatedAt;
    @FXML private TableColumn<TeachingTask, Void> colActions;
    @FXML private Pagination pagination;

    private final ObservableList<TeachingTask> tableData = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTeacherId.setCellValueFactory(new PropertyValueFactory<>("teacherId"));
        colClassId.setCellValueFactory(new PropertyValueFactory<>("classId"));
        colCourseId.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colActions.setCellFactory(param -> new TableCell<>() {
            {
                Button editBtn = new Button("编辑"); editBtn.getStyleClass().addAll("btn-edit", "btn-sm");
                Button deleteBtn = new Button("删除"); deleteBtn.getStyleClass().addAll("btn-delete", "btn-sm");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                HBox box = new HBox(8, editBtn, deleteBtn); box.setAlignment(Pos.CENTER); setGraphic(box);
            }
            @Override
            protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : getGraphic()); }
        });

        table.setItems(tableData);
        loadData();
    }

    private void loadData() {
        Task<PageResult<TeachingTask>> task = new Task<>() {
            @Override
            protected PageResult<TeachingTask> call() throws Exception {
                return TeachingTaskService.getPage(currentPage, pageSize, semesterField.getText());
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<TeachingTask> result = task.getValue();
            tableData.clear(); tableData.addAll(result.getRecords());
            totalItems = result.getTotal(); updatePagination();
        });
        task.setOnFailed(e -> CrudHelper.showError("加载失败"));
        new Thread(task).start();
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) totalItems / pageSize);
        if (pageCount < 1) pageCount = 1;
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(currentPage - 1);
        pagination.setPageFactory(pageIndex -> {
            if (pageIndex + 1 != currentPage) { currentPage = pageIndex + 1; loadData(); }
            return new Label("");
        });
    }

    @FXML
    private void onSearchKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) { currentPage = 1; loadData(); }
    }

    @FXML
    private void handleCreate() { showFormDialog("新增教学任务", null); }

    private void handleEdit(TeachingTask item) { showFormDialog("编辑教学任务", item); }

    private void showFormDialog(String title, TeachingTask existing) {
        Dialog<TeachingTask> dialog = new Dialog<>();
        dialog.setTitle(title);
        TextField teacherIdField = new TextField();
        TextField classIdField = new TextField();
        TextField courseIdField = new TextField();
        TextField semesterFieldLocal = new TextField(); semesterFieldLocal.setPromptText("如: 2025-1");

        if (existing != null) {
            teacherIdField.setText(String.valueOf(existing.getTeacherId()));
            classIdField.setText(String.valueOf(existing.getClassId()));
            courseIdField.setText(String.valueOf(existing.getCourseId()));
            semesterFieldLocal.setText(existing.getSemester());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12); grid.setPadding(new Insets(20));
        int r = 0;
        grid.add(new Label("教师ID:"), 0, r); grid.add(teacherIdField, 1, r++);
        grid.add(new Label("班级ID:"), 0, r); grid.add(classIdField, 1, r++);
        grid.add(new Label("课程ID:"), 0, r); grid.add(courseIdField, 1, r++);
        grid.add(new Label("学期:"), 0, r); grid.add(semesterFieldLocal, 1, r++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (teacherIdField.getText().isEmpty() || classIdField.getText().isEmpty()
                        || courseIdField.getText().isEmpty() || semesterFieldLocal.getText().isEmpty()) {
                    CrudHelper.showError("请填写必填项"); return null;
                }
                TeachingTask t = existing != null ? existing : new TeachingTask();
                t.setTeacherId(Integer.parseInt(teacherIdField.getText()));
                t.setClassId(Integer.parseInt(classIdField.getText()));
                t.setCourseId(Integer.parseInt(courseIdField.getText()));
                t.setSemester(semesterFieldLocal.getText());
                return t;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            Task<Void> saveTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    if (existing != null) TeachingTaskService.update(existing.getId(), result);
                    else TeachingTaskService.create(result);
                    return null;
                }
            };
            saveTask.setOnSucceeded(e -> { CrudHelper.showAlert(existing != null ? "更新成功" : "新增成功"); loadData(); });
            saveTask.setOnFailed(e -> CrudHelper.showError("操作失败"));
            new Thread(saveTask).start();
        });
    }

    private void handleDelete(TeachingTask item) {
        CrudHelper.confirmDelete(selected -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception { TeachingTaskService.delete(selected.getId()); return null; }
            };
            task.setOnSucceeded(e -> { CrudHelper.showAlert("删除成功"); loadData(); });
            task.setOnFailed(e -> CrudHelper.showError("删除失败"));
            new Thread(task).start();
        }, item);
    }
}
