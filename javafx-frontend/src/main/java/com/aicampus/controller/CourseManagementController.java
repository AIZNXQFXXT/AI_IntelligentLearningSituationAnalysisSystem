package com.aicampus.controller;

import com.aicampus.model.Course;
import com.aicampus.model.PageResult;
import com.aicampus.service.CourseService;
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

public class CourseManagementController {

    @FXML private ComboBox<String> typeFilter;
    @FXML private TextField keywordField;
    @FXML private TableView<Course> table;
    @FXML private TableColumn<Course, Integer> colId;
    @FXML private TableColumn<Course, String> colName;
    @FXML private TableColumn<Course, String> colType;
    @FXML private TableColumn<Course, Double> colCredit;
    @FXML private TableColumn<Course, String> colDescription;
    @FXML private TableColumn<Course, String> colStatus;
    @FXML private TableColumn<Course, Void> colActions;
    @FXML private Pagination pagination;

    private final ObservableList<Course> tableData = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

    @FXML
    public void initialize() {
        typeFilter.getItems().addAll("全部", "必修", "选修", "专业");
        typeFilter.setValue("全部");
        typeFilter.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> { currentPage = 1; loadData(); });

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(switch (cellData.getValue().getType()) {
            case "REQUIRED" -> "必修"; case "ELECTIVE" -> "选修"; case "MAJOR" -> "专业"; default -> cellData.getValue().getType();
        }));
        colCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus() == 1 ? "启用" : "停用"));

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

    private String getSelectedType() {
        String val = typeFilter.getValue();
        if (val == null || "全部".equals(val)) return null;
        return switch (val) { case "必修" -> "REQUIRED"; case "选修" -> "ELECTIVE"; case "专业" -> "MAJOR"; default -> null; };
    }

    private void loadData() {
        Task<PageResult<Course>> task = new Task<>() {
            @Override
            protected PageResult<Course> call() throws Exception {
                return CourseService.getPage(currentPage, pageSize, keywordField.getText(), getSelectedType());
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<Course> result = task.getValue();
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
    private void handleCreate() { showFormDialog("新增课程", null); }

    private void handleEdit(Course item) { showFormDialog("编辑课程", item); }

    private void showFormDialog(String title, Course existing) {
        Dialog<Course> dialog = new Dialog<>();
        dialog.setTitle(title);
        TextField nameField = new TextField();
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("必修", "选修", "专业");
        TextField creditField = new TextField();
        TextArea descArea = new TextArea(); descArea.setPrefRowCount(3);
        CheckBox statusCheck = new CheckBox("启用");

        if (existing != null) {
            nameField.setText(existing.getName());
            typeBox.setValue(switch (existing.getType()) { case "REQUIRED" -> "必修"; case "ELECTIVE" -> "选修"; case "MAJOR" -> "专业"; default -> existing.getType(); });
            creditField.setText(String.valueOf(existing.getCredit()));
            descArea.setText(existing.getDescription());
            statusCheck.setSelected(existing.getStatus() == 1);
        } else { statusCheck.setSelected(true); }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12); grid.setPadding(new Insets(20));
        int r = 0;
        grid.add(new Label("课程名:"), 0, r); grid.add(nameField, 1, r++);
        grid.add(new Label("类型:"), 0, r); grid.add(typeBox, 1, r++);
        grid.add(new Label("学分:"), 0, r); grid.add(creditField, 1, r++);
        grid.add(new Label("描述:"), 0, r); grid.add(descArea, 1, r++);
        grid.add(statusCheck, 1, r++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (nameField.getText().isEmpty() || typeBox.getValue() == null || creditField.getText().isEmpty()) {
                    CrudHelper.showError("请填写必填项"); return null;
                }
                Course c = existing != null ? existing : new Course();
                c.setName(nameField.getText());
                c.setType(switch (typeBox.getValue()) { case "必修" -> "REQUIRED"; case "选修" -> "ELECTIVE"; case "专业" -> "MAJOR"; default -> typeBox.getValue(); });
                c.setCredit(Double.parseDouble(creditField.getText()));
                c.setDescription(descArea.getText());
                c.setStatus(statusCheck.isSelected() ? 1 : 0);
                return c;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            Task<Void> saveTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    if (existing != null) CourseService.update(existing.getId(), result);
                    else CourseService.create(result);
                    return null;
                }
            };
            saveTask.setOnSucceeded(e -> { CrudHelper.showAlert(existing != null ? "更新成功" : "新增成功"); loadData(); });
            saveTask.setOnFailed(e -> CrudHelper.showError("操作失败"));
            new Thread(saveTask).start();
        });
    }

    private void handleDelete(Course item) {
        CrudHelper.confirmDelete(selected -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception { CourseService.delete(selected.getId()); return null; }
            };
            task.setOnSucceeded(e -> { CrudHelper.showAlert("删除成功"); loadData(); });
            task.setOnFailed(e -> CrudHelper.showError("删除失败"));
            new Thread(task).start();
        }, item);
    }
}
