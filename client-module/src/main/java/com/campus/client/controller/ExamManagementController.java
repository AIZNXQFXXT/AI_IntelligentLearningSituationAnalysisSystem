package com.campus.client.controller;

import com.campus.client.model.ClassInfo;
import com.campus.common.dto.ExamDTO;
import com.campus.client.model.PageResult;
import com.campus.client.service.ClassService;
import com.campus.client.service.ExamService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamManagementController {

    @FXML private TextField semesterField;
    @FXML private TableView<ExamDTO> table;
    @FXML private TableColumn<ExamDTO, String> colName;
    @FXML private TableColumn<ExamDTO, String> colType;
    @FXML private TableColumn<ExamDTO, String> colSemester;
    @FXML private TableColumn<ExamDTO, String> colClassId;
    @FXML private TableColumn<ExamDTO, String> colExamDate;
    @FXML private TableColumn<ExamDTO, String> colArchived;
    @FXML private TableColumn<ExamDTO, Void> colActions;
    @FXML private Pagination pagination;

    private final ObservableList<ExamDTO> tableData = FXCollections.observableArrayList();
    private final Map<Integer, String> classMap = new HashMap<>();
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(mapExamType(cellData.getValue().getType())));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colClassId.setCellValueFactory(data ->
                new SimpleStringProperty(classMap.getOrDefault(data.getValue().getClassId().intValue(), "未知")));
        colExamDate.setCellValueFactory(new PropertyValueFactory<>("examDate"));
        colArchived.setCellValueFactory(cellData -> new SimpleStringProperty(Integer.valueOf(1).equals(cellData.getValue().getIsArchived()) ? "已归档" : "未归档"));

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
                ExamDTO exam = getTableView().getItems().get(getIndex());
                if (getGraphic() instanceof HBox && ((HBox) getGraphic()).getChildren().get(1) instanceof Button) {
                    Button ab = (Button) ((HBox) getGraphic()).getChildren().get(1);
                    ab.setVisible(!Integer.valueOf(1).equals(exam.getIsArchived())); ab.setManaged(!Integer.valueOf(1).equals(exam.getIsArchived()));
                }
            }
        });

        table.setItems(tableData);
        loadClassMap();
        loadData();
    }

    private void loadClassMap() {
        Task<List<ClassInfo>> task = new Task<>() {
            @Override
            protected List<ClassInfo> call() throws Exception {
                return ClassService.getAll();
            }
        };
        task.setOnSucceeded(e -> {
            classMap.clear();
            for (ClassInfo c : task.getValue()) {
                classMap.put(c.getId(), c.getClassName());
            }
            table.refresh();
        });
        task.setOnFailed(e -> {});
        AppExecutors.submit(task::run);
    }

    private String mapExamType(String type) {
        switch (type) {
            case "MOCK": return "月考";
            case "MIDTERM": return "期中";
            case "FINAL": return "期末";
            case "RETEST": return "补考";
            default: return type;
        }
    }

    private String mapExamTypeReverse(String type) {
        switch (type) {
            case "月考": return "MOCK";
            case "期中": return "MIDTERM";
            case "期末": return "FINAL";
            case "补考": return "RETEST";
            default: return type;
        }
    }

    private void loadData() {
        Task<PageResult<ExamDTO>> task = new Task<>() {
            @Override
            protected PageResult<ExamDTO> call() throws Exception {
                return ExamService.getPage(currentPage, pageSize, semesterField.getText());
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<ExamDTO> result = task.getValue();
            tableData.clear(); tableData.addAll(result.getRecords());
            totalItems = result.getTotal(); updatePagination();
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
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

    private void handleEdit(ExamDTO item) { showFormDialog("编辑考试", item); }

    private void showFormDialog(String title, ExamDTO existing) {
        Dialog<ExamDTO> dialog = new Dialog<>();
        dialog.setTitle(title);
        TextField nameField = new TextField();
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("月考", "期中", "期末", "补考");
        TextField semesterFieldLocal = new TextField(); semesterFieldLocal.setPromptText("如: 2025-1");
        ComboBox<String> classBox = new ComboBox<>();
        classBox.setPromptText("选择班级");
        Task<List<ClassInfo>> loadClasses = new Task<>() {
            @Override protected List<ClassInfo> call() throws Exception { return ClassService.getAll(); }
        };
        loadClasses.setOnSucceeded(ev -> {
            for (ClassInfo c : loadClasses.getValue()) {
                classBox.getItems().add(c.getId() + " - " + c.getClassName());
            }
            if (existing != null && existing.getClassId() > 0) {
                for (String item : classBox.getItems()) {
                    if (item.startsWith(existing.getClassId() + " - ")) {
                        classBox.setValue(item);
                        break;
                    }
                }
            }
        });
        AppExecutors.submit(loadClasses::run);
        DatePicker examDatePicker = new DatePicker();

        if (existing != null) {
            nameField.setText(existing.getName());
            typeBox.setValue(mapExamType(existing.getType()));
            semesterFieldLocal.setText(existing.getSemester());
            if (existing.getExamDate() != null) {
                examDatePicker.setValue(existing.getExamDate());
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12); grid.setPadding(new Insets(20));
        int r = 0;
        grid.add(new Label("考试名称:"), 0, r); grid.add(nameField, 1, r++);
        grid.add(new Label("类型:"), 0, r); grid.add(typeBox, 1, r++);
        grid.add(new Label("学期:"), 0, r); grid.add(semesterFieldLocal, 1, r++);
        grid.add(new Label("班级:"), 0, r); grid.add(classBox, 1, r++);
        grid.add(new Label("考试日期:"), 0, r); grid.add(examDatePicker, 1, r++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (nameField.getText().isEmpty() || typeBox.getValue() == null || semesterFieldLocal.getText().isEmpty()) {
                    CrudHelper.showError("请填写必填项"); return null;
                }
                ExamDTO e = existing != null ? existing : new ExamDTO();
                e.setName(nameField.getText());
                e.setType(mapExamTypeReverse(typeBox.getValue()));
                e.setSemester(semesterFieldLocal.getText());
                String classVal = classBox.getValue();
                if (classVal != null) e.setClassId(Long.parseLong(classVal.split(" - ")[0]));
                e.setExamDate(examDatePicker.getValue());
                return e;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            Task<Void> saveTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    if (existing != null) ExamService.update(existing.getId().intValue(), result);
                    else ExamService.create(result);
                    return null;
                }
            };
            saveTask.setOnSucceeded(ev -> { CrudHelper.showAlert(existing != null ? "更新成功" : "新增成功"); loadData(); });
            saveTask.setOnFailed(ev -> CrudHelper.showError("操作失败"));
            AppExecutors.submit(saveTask::run);
        });
    }

    private void handleArchive(ExamDTO item) {
        CrudHelper.confirmDelete(selected -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception { ExamService.archive(selected.getId().intValue()); return null; }
            };
            task.setOnSucceeded(e -> { CrudHelper.showAlert("归档成功"); loadData(); });
            task.setOnFailed(e -> CrudHelper.showError("归档失败"));
            AppExecutors.submit(task::run);
        }, item);
    }

    private void handleDelete(ExamDTO item) {
        CrudHelper.confirmDelete(selected -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception { ExamService.delete(selected.getId().intValue()); return null; }
            };
            task.setOnSucceeded(e -> { CrudHelper.showAlert("删除成功"); loadData(); });
            task.setOnFailed(e -> CrudHelper.showError("删除失败"));
            AppExecutors.submit(task::run);
        }, item);
    }
}
