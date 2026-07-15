package com.aicampus.controller;

import com.aicampus.model.Exam;
import com.aicampus.model.PageResult;
import com.aicampus.service.ExamService;
import com.aicampus.util.CrudHelper;
import javafx.beans.property.SimpleStringProperty;
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

public class ExamManagementController {

    @FXML private TextField semesterField;
    @FXML private TableView<Exam> table;
    @FXML private TableColumn<Exam, Integer> colId;
    @FXML private TableColumn<Exam, String> colName;
    @FXML private TableColumn<Exam, String> colType;
    @FXML private TableColumn<Exam, String> colSemester;
    @FXML private TableColumn<Exam, Integer> colClassId;
    @FXML private TableColumn<Exam, String> colExamDate;
    @FXML private TableColumn<Exam, String> colArchived;
    @FXML private TableColumn<Exam, Void> colActions;
    @FXML private Pagination pagination;

    private final ObservableList<Exam> tableData = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(switch (cellData.getValue().getType()) {
            case "MOCK" -> "月考"; case "MIDTERM" -> "期中"; case "FINAL" -> "期末"; case "RETEST" -> "补考"; default -> cellData.getValue().getType();
        }));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colClassId.setCellValueFactory(new PropertyValueFactory<>("classId"));
        colExamDate.setCellValueFactory(new PropertyValueFactory<>("examDate"));
        colArchived.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIsArchived() ? "已归档" : "未归档"));

        colActions.setCellFactory(param -> new TableCell<>() {
            {
                Button editBtn = new Button("编辑"); editBtn.getStyleClass().addAll("btn-edit", "btn-sm");
                Button archiveBtn = new Button("归档"); archiveBtn.getStyleClass().addAll("btn-primary", "btn-sm");
                Button deleteBtn = new Button("删除"); deleteBtn.getStyleClass().addAll("btn-delete", "btn-sm");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                archiveBtn.setOnAction(e -> handleArchive(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                HBox box = new HBox(8, editBtn, archiveBtn, deleteBtn); box.setAlignment(Pos.CENTER); setGraphic(box);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Exam exam = getTableView().getItems().get(getIndex());
                if (getGraphic() instanceof HBox hbox && hbox.getChildren().get(1) instanceof Button ab) {
                    ab.setVisible(!exam.getIsArchived()); ab.setManaged(!exam.getIsArchived());
                }
            }
        });

        table.setItems(tableData);
        loadData();
    }

    private void loadData() {
        Task<PageResult<Exam>> task = new Task<>() {
            @Override
            protected PageResult<Exam> call() throws Exception {
                return ExamService.getPage(currentPage, pageSize, semesterField.getText());
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<Exam> result = task.getValue();
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
    private void handleCreate() { showFormDialog("新增考试", null); }

    private void handleEdit(Exam item) { showFormDialog("编辑考试", item); }

    private void showFormDialog(String title, Exam existing) {
        Dialog<Exam> dialog = new Dialog<>();
        dialog.setTitle(title);
        TextField nameField = new TextField();
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("月考", "期中", "期末", "补考");
        TextField semesterFieldLocal = new TextField(); semesterFieldLocal.setPromptText("如: 2025-1");
        TextField classIdField = new TextField();
        TextField examDateField = new TextField(); examDateField.setPromptText("YYYY-MM-DD");

        if (existing != null) {
            nameField.setText(existing.getName());
            typeBox.setValue(switch (existing.getType()) { case "MOCK" -> "月考"; case "MIDTERM" -> "期中"; case "FINAL" -> "期末"; case "RETEST" -> "补考"; default -> existing.getType(); });
            semesterFieldLocal.setText(existing.getSemester());
            if (existing.getClassId() > 0) classIdField.setText(String.valueOf(existing.getClassId()));
            examDateField.setText(existing.getExamDate());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12); grid.setPadding(new Insets(20));
        int r = 0;
        grid.add(new Label("考试名称:"), 0, r); grid.add(nameField, 1, r++);
        grid.add(new Label("类型:"), 0, r); grid.add(typeBox, 1, r++);
        grid.add(new Label("学期:"), 0, r); grid.add(semesterFieldLocal, 1, r++);
        grid.add(new Label("班级ID:"), 0, r); grid.add(classIdField, 1, r++);
        grid.add(new Label("考试日期:"), 0, r); grid.add(examDateField, 1, r++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (nameField.getText().isEmpty() || typeBox.getValue() == null || semesterFieldLocal.getText().isEmpty()) {
                    CrudHelper.showError("请填写必填项"); return null;
                }
                Exam e = existing != null ? existing : new Exam();
                e.setName(nameField.getText());
                e.setType(switch (typeBox.getValue()) { case "月考" -> "MOCK"; case "期中" -> "MIDTERM"; case "期末" -> "FINAL"; case "补考" -> "RETEST"; default -> typeBox.getValue(); });
                e.setSemester(semesterFieldLocal.getText());
                if (!classIdField.getText().isEmpty()) e.setClassId(Integer.parseInt(classIdField.getText()));
                e.setExamDate(examDateField.getText());
                return e;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            Task<Void> saveTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    if (existing != null) ExamService.update(existing.getId(), result);
                    else ExamService.create(result);
                    return null;
                }
            };
            saveTask.setOnSucceeded(ev -> { CrudHelper.showAlert(existing != null ? "更新成功" : "新增成功"); loadData(); });
            saveTask.setOnFailed(ev -> CrudHelper.showError("操作失败"));
            new Thread(saveTask).start();
        });
    }

    private void handleArchive(Exam item) {
        CrudHelper.confirmDelete(selected -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception { ExamService.archive(selected.getId()); return null; }
            };
            task.setOnSucceeded(e -> { CrudHelper.showAlert("归档成功"); loadData(); });
            task.setOnFailed(e -> CrudHelper.showError("归档失败"));
            new Thread(task).start();
        }, item);
    }

    private void handleDelete(Exam item) {
        CrudHelper.confirmDelete(selected -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception { ExamService.delete(selected.getId()); return null; }
            };
            task.setOnSucceeded(e -> { CrudHelper.showAlert("删除成功"); loadData(); });
            task.setOnFailed(e -> CrudHelper.showError("删除失败"));
            new Thread(task).start();
        }, item);
    }
}
