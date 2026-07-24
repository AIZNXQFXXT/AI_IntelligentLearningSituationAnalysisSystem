package com.campus.client.controller;

import com.campus.client.model.AiComment;
import com.campus.client.model.ClassInfo;
import com.campus.client.service.ClassService;
import com.campus.client.service.CommentService;
import com.campus.client.service.SemesterService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.ClientLogger;
import com.campus.client.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;

public class TeacherCommentController {

    @FXML private TableView<AiComment> table;
    @FXML private TableColumn<AiComment, String> colStudentName;
    @FXML private TableColumn<AiComment, String> colClassName;
    @FXML private TableColumn<AiComment, String> colSemester;
    @FXML private TableColumn<AiComment, String> colContent;
    @FXML private TableColumn<AiComment, Boolean> colEdited;
    @FXML private TableColumn<AiComment, String> colCreatedAt;
    @FXML private TableColumn<AiComment, Void> colAction;
    @FXML private ComboBox<ClassInfo> classCombo;
    @FXML private ComboBox<String> semesterCombo;
    @FXML private TextField searchField;

    private final ObservableList<AiComment> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        semesterCombo.setOnAction(e -> loadData());
        classCombo.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, val) -> loadData());
        loadClasses();
        loadSemesters();

        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colContent.setCellValueFactory(new PropertyValueFactory<>("content"));
        colEdited.setCellValueFactory(new PropertyValueFactory<>("teacherEdited"));
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

    private void loadClasses() {
        Task<List<ClassInfo>> task = new Task<>() {
            @Override protected List<ClassInfo> call() throws Exception {
                try {
                    return ClassService.getMyClasses().getRecords();
                } catch (Exception e) {
                    ClientLogger.warning("[loadClasses] getMyClasses failed: " + e.getMessage());
                    return ClassService.getAll();
                }
            }
        };
        task.setOnSucceeded(e -> {
            classCombo.setItems(FXCollections.observableArrayList(task.getValue()));
            classCombo.getSelectionModel().selectFirst();
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ClientLogger.severe("[loadClasses] 加载班级列表失败: "
                    + (ex != null ? ex.getClass().getSimpleName() + ": " + ex.getMessage() : "unknown"));
            CrudHelper.showError("加载班级列表失败");
        });
        AppExecutors.submit(task::run);
    }

    private void loadSemesters() {
        Task<List<String>> task = new Task<>() {
            @Override protected List<String> call() throws Exception {
                return SemesterService.getAllSemesters();
            }
        };
        task.setOnSucceeded(e -> {
            semesterCombo.setItems(FXCollections.observableArrayList(task.getValue()));
            semesterCombo.getSelectionModel().selectFirst();
        });
        task.setOnFailed(e -> {
            semesterCombo.setItems(FXCollections.observableArrayList("2025-1", "2024-2", "2024-1"));
            semesterCombo.getSelectionModel().selectFirst();
        });
        AppExecutors.submit(task::run);
    }

    @FXML
    private void handleSearch() {
        loadData();
    }

    private void loadData() {
        ClassInfo cls = classCombo.getValue();
        String semester = semesterCombo.getValue();
        if (cls == null) return;
        int classId = cls.getId();
        final String keyword = searchField != null && searchField.getText() != null
                ? searchField.getText().trim() : "";
        Task<List<AiComment>> task = new Task<>() {
            @Override protected List<AiComment> call() throws Exception {
                return CommentService.getPage(1, 200, classId, semester, keyword).getRecords();
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
        ClassInfo cls = classCombo.getValue();
        String semester = semesterCombo.getValue();
        if (semester == null) {
            CrudHelper.showError("请先选择学期");
            return;
        }
        if (cls == null) {
            CrudHelper.showError("请先选择班级");
            return;
        }
        int classId = cls.getId();
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                return CommentService.batchGenerate(classId, semester);
            }
        };
        task.setOnSucceeded(e -> {
            CrudHelper.showAlert("批量生成已启动成功");
            loadData();
        });
        task.setOnFailed(e -> CrudHelper.showError("批量生成失败"));
        AppExecutors.submit(task::run);
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
            Task<AiComment> task = new Task<>() {
                @Override protected AiComment call() throws Exception {
                    return CommentService.updateComment(c.getId(), content);
                }
            };
            task.setOnSucceeded(e -> {
                AiComment updated = task.getValue();
                if (updated != null) {
                    c.setContent(updated.getContent());
                    c.setTeacherEdited(updated.isTeacherEdited());
                }
                table.refresh();
                CrudHelper.showAlert("保存成功");
            });
            task.setOnFailed(e -> CrudHelper.showError("保存失败"));
            AppExecutors.submit(task::run);
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
