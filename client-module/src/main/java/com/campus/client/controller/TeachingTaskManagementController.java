package com.campus.client.controller;

import com.campus.client.model.ClassInfo;
import com.campus.client.model.Course;
import com.campus.client.model.PageResult;
import com.campus.client.model.Teacher;
import com.campus.client.model.TeachingTask;
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
    @FXML private TableView<TeachingTask> table;
    @FXML private TableColumn<TeachingTask, String> colTeacherId;
    @FXML private TableColumn<TeachingTask, String> colClassId;
    @FXML private TableColumn<TeachingTask, String> colCourseId;
    @FXML private TableColumn<TeachingTask, String> colSemester;
    @FXML private TableColumn<TeachingTask, String> colCreatedAt;
    @FXML private TableColumn<TeachingTask, Void> colActions;
    @FXML private Pagination pagination;

    private final ObservableList<TeachingTask> tableData = FXCollections.observableArrayList();
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
        // 分页工厂只注册一次：用户点页码时由它触发加载
        pagination.setPageFactory(this::buildPage);
        loadMaps();
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

    private void loadMaps() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (Teacher t : TeacherService.getAll())
                    teacherMap.put(t.getId(), t.getName() + " (" + t.getTeacherNo() + ")");
                for (ClassInfo c : ClassService.getAll())
                    classMap.put(c.getId(), c.getClassName());
                for (Course c : CourseService.getAll())
                    courseMap.put(c.getId(), c.getName());
                return null;
            }
        };
        task.setOnSucceeded(e -> table.refresh());
        task.setOnFailed(e -> {});
        AppExecutors.submit(task::run);
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
    private void handleCreate() { showFormDialog("新增教学任务", null); }

    private void handleEdit(TeachingTask item) { showFormDialog("编辑教学任务", item); }

    private void showFormDialog(String title, TeachingTask existing) {
        Dialog<TeachingTask> dialog = new Dialog<>();
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
            if (existing != null) selectByPrefix(teacherBox, existing.getTeacherId());
        });

        Task<List<ClassInfo>> loadClasses = new Task<>() {
            @Override protected List<ClassInfo> call() throws Exception { return ClassService.getAll(); }
        };
        loadClasses.setOnSucceeded(ev -> {
            for (ClassInfo c : loadClasses.getValue())
                classBox.getItems().add(c.getId() + " - " + c.getClassName());
            if (existing != null) selectByPrefix(classBox, existing.getClassId());
        });

        Task<List<Course>> loadCourses = new Task<>() {
            @Override protected List<Course> call() throws Exception { return CourseService.getAll(); }
        };
        loadCourses.setOnSucceeded(ev -> {
            for (Course c : loadCourses.getValue())
                courseBox.getItems().add(c.getId() + " - " + c.getName());
            if (existing != null) selectByPrefix(courseBox, existing.getCourseId());
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
                TeachingTask t = existing != null ? existing : new TeachingTask();
                t.setTeacherId(Integer.parseInt(teacherBox.getValue().split(" - ")[0]));
                t.setClassId(Integer.parseInt(classBox.getValue().split(" - ")[0]));
                t.setCourseId(Integer.parseInt(courseBox.getValue().split(" - ")[0]));
                t.setSemester(semesterCombo.getValue());
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
            AppExecutors.submit(saveTask::run);
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
