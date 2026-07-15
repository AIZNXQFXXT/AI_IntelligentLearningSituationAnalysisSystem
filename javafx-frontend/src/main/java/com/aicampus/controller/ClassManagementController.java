package com.aicampus.controller;

import com.aicampus.model.ClassInfo;
import com.aicampus.model.PageResult;
import com.aicampus.service.ClassService;
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

public class ClassManagementController {

    @FXML private TextField keywordField;
    @FXML private TableView<ClassInfo> table;
    @FXML private TableColumn<ClassInfo, Integer> colId;
    @FXML private TableColumn<ClassInfo, String> colGrade;
    @FXML private TableColumn<ClassInfo, String> colClassName;
    @FXML private TableColumn<ClassInfo, Integer> colHeadTeacherId;
    @FXML private TableColumn<ClassInfo, Integer> colStudentCount;
    @FXML private TableColumn<ClassInfo, String> colCreatedAt;
    @FXML private TableColumn<ClassInfo, Void> colActions;
    @FXML private Pagination pagination;

    private final ObservableList<ClassInfo> tableData = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("grade"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colHeadTeacherId.setCellValueFactory(new PropertyValueFactory<>("headTeacherId"));
        colStudentCount.setCellValueFactory(new PropertyValueFactory<>("studentCount"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colActions.setCellFactory(param -> new TableCell<>() {
            {
                Button editBtn = new Button("编辑");
                editBtn.getStyleClass().addAll("btn-edit", "btn-sm");
                Button deleteBtn = new Button("删除");
                deleteBtn.getStyleClass().addAll("btn-delete", "btn-sm");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                HBox box = new HBox(8, editBtn, deleteBtn);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });

        table.setItems(tableData);
        loadData();
    }

    private void loadData() {
        String keyword = keywordField.getText();
        Task<PageResult<ClassInfo>> task = new Task<>() {
            @Override
            protected PageResult<ClassInfo> call() throws Exception {
                return ClassService.getPage(currentPage, pageSize, keyword);
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<ClassInfo> result = task.getValue();
            tableData.clear();
            tableData.addAll(result.getRecords());
            totalItems = result.getTotal();
            updatePagination();
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
            if (pageIndex + 1 != currentPage) {
                currentPage = pageIndex + 1;
                loadData();
            }
            return new Label("");
        });
    }

    @FXML
    private void onSearchKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            currentPage = 1;
            loadData();
        }
    }

    @FXML
    private void handleCreate() {
        showFormDialog("新增班级", null);
    }

    private void handleEdit(ClassInfo item) {
        showFormDialog("编辑班级", item);
    }

    private void showFormDialog(String title, ClassInfo existing) {
        Dialog<ClassInfo> dialog = new Dialog<>();
        dialog.setTitle(title);

        TextField gradeField = new TextField();
        gradeField.setPromptText("如: 2024级");
        if (existing != null) gradeField.setText(existing.getGrade());

        TextField classNameField = new TextField();
        classNameField.setPromptText("如: 计算机1班");
        if (existing != null) classNameField.setText(existing.getClassName());

        TextField headTeacherIdField = new TextField();
        headTeacherIdField.setPromptText("班主任ID");
        if (existing != null && existing.getHeadTeacherId() > 0) {
            headTeacherIdField.setText(String.valueOf(existing.getHeadTeacherId()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.add(new Label("年级:"), 0, 0);
        grid.add(gradeField, 1, 0);
        grid.add(new Label("班级名:"), 0, 1);
        grid.add(classNameField, 1, 1);
        grid.add(new Label("班主任ID:"), 0, 2);
        grid.add(headTeacherIdField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (gradeField.getText().isEmpty() || classNameField.getText().isEmpty()) {
                    CrudHelper.showError("请填写必填项");
                    return null;
                }
                ClassInfo info = existing != null ? existing : new ClassInfo();
                if (existing != null) info.setId(existing.getId());
                info.setGrade(gradeField.getText());
                info.setClassName(classNameField.getText());
                String headId = headTeacherIdField.getText();
                if (!headId.isEmpty()) info.setHeadTeacherId(Integer.parseInt(headId));
                return info;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            Task<Void> saveTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    if (existing != null) {
                        ClassService.update(existing.getId(), result);
                    } else {
                        ClassService.create(result);
                    }
                    return null;
                }
            };
            saveTask.setOnSucceeded(e -> {
                CrudHelper.showAlert(existing != null ? "更新成功" : "新增成功");
                loadData();
            });
            saveTask.setOnFailed(e -> CrudHelper.showError("操作失败"));
            new Thread(saveTask).start();
        });
    }

    private void handleDelete(ClassInfo item) {
        CrudHelper.confirmDelete(selected -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    ClassService.delete(selected.getId());
                    return null;
                }
            };
            task.setOnSucceeded(e -> {
                CrudHelper.showAlert("删除成功");
                loadData();
            });
            task.setOnFailed(e -> CrudHelper.showError("删除失败"));
            new Thread(task).start();
        }, item);
    }
}
