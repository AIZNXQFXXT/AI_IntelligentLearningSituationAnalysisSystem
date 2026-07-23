package com.campus.client.controller;

import com.campus.client.model.ClassInfo;
import com.campus.client.model.Exam;
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
    @FXML private TableView<Exam> table;
    @FXML private TableColumn<Exam, String> colName;
    @FXML private TableColumn<Exam, String> colType;
    @FXML private TableColumn<Exam, String> colSemester;
    @FXML private TableColumn<Exam, String> colClassId;
    @FXML private TableColumn<Exam, String> colExamDate;
    @FXML private TableColumn<Exam, String> colArchived;
    @FXML private TableColumn<Exam, Void> colActions;
    @FXML private Pagination pagination;

    private final ObservableList<Exam> tableData = FXCollections.observableArrayList();
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
                new SimpleStringProperty(classMap.getOrDefault(data.getValue().getClassId(), "未知")));
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
                if (getGraphic() instanceof HBox && ((HBox) getGraphic()).getChildren().get(1) instanceof Button) {
                    Button ab = (Button) ((HBox) getGraphic()).getChildren().get(1);
                    ab.setVisible(!exam.getIsArchived()); ab.setManaged(!exam.getIsArchived());
                }
            }
        });

        table.setItems(tableData);
        // 分页工厂只注册一次：用户点页码时由它触发加载
        pagination.setPageFactory(this::buildPage);
        loadClassMap();
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
        Task<PageResult<Exam>> task = new Task<>() {
            @Override
            protected PageResult<Exam> call() throws Exception {
                return ExamService.getPage(currentPage, pageSize, semesterField.getText());
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<Exam> result = task.getValue();
            tableData.clear(); tableData.addAll(result.getRecords());
            totalItems = result.getTotal();
            int pageCount = (int) Math.ceil((double) totalItems / pageSize);
            if (pageCount < 1) pageCount = 1;
            pagination.setPageCount(pageCount);
            // 此处不再调用 setCurrentPageIndex / setPageFactory，否则异步响应会把指针拽回本次页，触发连环重载
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }

    @FXML
    private void onSearchKeyPressed(javafx.scene.input.KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
            currentPage = 1;
            pagination.setCurrentPageIndex(0);
            loadData();
        }
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
        ComboBox<String> semesterFieldLocal = new ComboBox<>();
        int currentYear = java.time.Year.now().getValue();
        semesterFieldLocal.getItems().addAll(currentYear + "-1", currentYear + "-2");
        semesterFieldLocal.setPromptText("选择学期");
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
            semesterFieldLocal.setValue(existing.getSemester());
            if (existing.getExamDate() != null && !existing.getExamDate().isEmpty()) {
                examDatePicker.setValue(java.time.LocalDate.parse(existing.getExamDate()));
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
                if (nameField.getText().isEmpty() || typeBox.getValue() == null || semesterFieldLocal.getValue() == null) {
                    CrudHelper.showError("请填写必填项"); return null;
                }
                Exam e = existing != null ? existing : new Exam();
                e.setName(nameField.getText());
                e.setType(mapExamTypeReverse(typeBox.getValue()));
                e.setSemester(semesterFieldLocal.getValue());
                String classVal = classBox.getValue();
                if (classVal != null) e.setClassId(Integer.parseInt(classVal.split(" - ")[0]));
                e.setExamDate(examDatePicker.getValue() != null ? examDatePicker.getValue().toString() : "");
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
            AppExecutors.submit(saveTask::run);
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
            AppExecutors.submit(task::run);
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
            AppExecutors.submit(task::run);
        }, item);
    }
}
