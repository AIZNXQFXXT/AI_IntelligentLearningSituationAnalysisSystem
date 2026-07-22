package com.campus.client.controller;

import com.campus.client.model.ClassInfo;
import com.campus.common.dto.CourseDTO;
import com.campus.client.model.Teacher;
import com.campus.common.dto.TeachingTaskDTO;
import com.campus.client.service.ClassService;
import com.campus.client.service.CourseService;
import com.campus.client.service.TeacherService;
import com.campus.client.service.TeachingTaskService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import com.campus.client.util.TableUtils;
import com.campus.client.model.PageResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.Year;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class TeachingTaskManagementController {

    @FXML private TextField semesterField;
    @FXML private TableView<TeachingTaskDTO> table;
    @FXML private TableColumn<TeachingTaskDTO, String> colTeacherId;
    @FXML private TableColumn<TeachingTaskDTO, String> colClassId;
    @FXML private TableColumn<TeachingTaskDTO, String> colCourseId;
    @FXML private TableColumn<TeachingTaskDTO, String> colSemester;
    @FXML private TableColumn<TeachingTaskDTO, String> colCreatedAt;
    @FXML private TableColumn<TeachingTaskDTO, Void> colActions;
    @FXML private Pagination pagination;

    private final ObservableList<TeachingTaskDTO> tableData = FXCollections.observableArrayList();
    private final Map<Integer, String> teacherMap = new HashMap<>();
    private final Map<Integer, String> classMap = new HashMap<>();
    private final Map<Integer, String> courseMap = new HashMap<>();
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

    @FXML
    public void initialize() {
        colTeacherId.setCellValueFactory(data ->
                new SimpleStringProperty(teacherMap.getOrDefault(data.getValue().getTeacherId(), "未知")));
        colClassId.setCellValueFactory(data ->
                new SimpleStringProperty(classMap.getOrDefault(data.getValue().getClassId(), "未知")));
        colCourseId.setCellValueFactory(data ->
                new SimpleStringProperty(courseMap.getOrDefault(data.getValue().getCourseId(), "未知")));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        TableUtils.setupActionColumn(colActions, this::handleEdit, this::handleDelete);

        table.setItems(tableData);
        loadMaps();
        loadData();
    }

    private void loadMaps() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (Teacher t : TeacherService.getAll())
                    teacherMap.put(t.getId(), t.getName() + " (" + t.getTeacherNo() + ")");
                for (ClassInfo c : ClassService.getAll())
                    classMap.put(c.getId(), c.getClassName());
                for (CourseDTO c : CourseService.getAll())
                    courseMap.put(c.getId().intValue(), c.getName());
                return null;
            }
        };
        task.setOnSucceeded(e -> table.refresh());
        task.setOnFailed(e -> {});
        AppExecutors.submit(task::run);
    }

    private void loadData() {
        Task<PageResult<TeachingTaskDTO>> task = new Task<>() {
            @Override
            protected PageResult<TeachingTaskDTO> call() throws Exception {
                return TeachingTaskService.getPage(currentPage, pageSize, semesterField.getText());
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<TeachingTaskDTO> result = task.getValue();
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
    private void handleCreate() { showFormDialog("新增教学任务", null); }

    private void handleEdit(TeachingTaskDTO item) { showFormDialog("编辑教学任务", item); }

    private void showFormDialog(String title, TeachingTaskDTO existing) {
        Dialog<TeachingTaskDTO> dialog = new Dialog<>();
        dialog.setTitle(title);
        ComboBox<String> teacherBox = new ComboBox<>();
        teacherBox.setPromptText("选择教师");
        ComboBox<String> classBox = new ComboBox<>();
        classBox.setPromptText("选择班级");
        ComboBox<String> courseBox = new ComboBox<>();
        courseBox.setPromptText("选择课程");
        ComboBox<String> semesterCombo = new ComboBox<>();
        int year = Year.now().getValue();
        semesterCombo.setItems(FXCollections.observableArrayList(year + "-1", year + "-2"));
        semesterCombo.getSelectionModel().selectFirst();

        Task<List<Teacher>> loadTeachers = new Task<>() {
            @Override protected List<Teacher> call() throws Exception { return TeacherService.getAll(); }
        };
        loadTeachers.setOnSucceeded(ev -> {
            for (Teacher t : loadTeachers.getValue())
                teacherBox.getItems().add(t.getId() + " - " + t.getTeacherNo() + " - " + t.getName());
            if (existing != null) selectByPrefix(teacherBox, existing.getTeacherId().intValue());
        });

        Task<List<ClassInfo>> loadClasses = new Task<>() {
            @Override protected List<ClassInfo> call() throws Exception { return ClassService.getAll(); }
        };
        loadClasses.setOnSucceeded(ev -> {
            for (ClassInfo c : loadClasses.getValue())
                classBox.getItems().add(c.getId() + " - " + c.getClassName());
            if (existing != null) selectByPrefix(classBox, existing.getClassId().intValue());
        });

        Task<List<CourseDTO>> loadCourses = new Task<>() {
            @Override protected List<CourseDTO> call() throws Exception { return CourseService.getAll(); }
        };
        loadCourses.setOnSucceeded(ev -> {
            for (CourseDTO c : loadCourses.getValue())
                courseBox.getItems().add(c.getId() + " - " + c.getName());
            if (existing != null) selectByPrefix(courseBox, existing.getCourseId().intValue());
        });

        AppExecutors.submit(loadTeachers::run);
        AppExecutors.submit(loadClasses::run);
        AppExecutors.submit(loadCourses::run);

        if (existing != null) semesterCombo.setValue(existing.getSemester());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(12); grid.setPadding(new Insets(20));
        int r = 0;
        grid.add(new Label("教师:"), 0, r); grid.add(teacherBox, 1, r++);
        grid.add(new Label("班级:"), 0, r); grid.add(classBox, 1, r++);
        grid.add(new Label("课程:"), 0, r); grid.add(courseBox, 1, r++);
        grid.add(new Label("学期:"), 0, r); grid.add(semesterCombo, 1, r++);

        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (teacherBox.getValue() == null || classBox.getValue() == null
                        || courseBox.getValue() == null || semesterCombo.getValue() == null) {
                    CrudHelper.showError("请填写必填项"); return null;
                }
                TeachingTaskDTO t = existing != null ? existing : new TeachingTaskDTO();
                t.setTeacherId(Long.parseLong(teacherBox.getValue().split(" - ")[0]));
                t.setClassId(Long.parseLong(classBox.getValue().split(" - ")[0]));
                t.setCourseId(Long.parseLong(courseBox.getValue().split(" - ")[0]));
                t.setSemester(semesterCombo.getValue());
                return t;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            Task<Void> saveTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    if (existing != null) TeachingTaskService.update(existing.getId().intValue(), result);
                    else TeachingTaskService.create(result);
                    return null;
                }
            };
            saveTask.setOnSucceeded(e -> { CrudHelper.showAlert(existing != null ? "更新成功" : "新增成功"); loadData(); });
            saveTask.setOnFailed(e -> CrudHelper.showError("操作失败"));
            AppExecutors.submit(saveTask::run);
        });
    }

    private void handleDelete(TeachingTaskDTO item) {
        CrudHelper.confirmDelete(selected -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception { TeachingTaskService.delete(selected.getId().intValue()); return null; }
            };
            task.setOnSucceeded(e -> { CrudHelper.showAlert("删除成功"); loadData(); });
            task.setOnFailed(e -> CrudHelper.showError("删除失败"));
            AppExecutors.submit(task::run);
        }, item);
    }

    private static void selectByPrefix(ComboBox<String> box, int id) {
        String prefix = id + " - ";
        for (String item : box.getItems()) {
            if (item.startsWith(prefix)) { box.setValue(item); break; }
        }
    }
}
