package com.aicampus.controller;

import com.aicampus.model.*;
import com.aicampus.service.*;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;

public class ScoreEntryController {

    @FXML private ComboBox<Exam> examCombo;
    @FXML private ComboBox<Course> courseCombo;
    @FXML private ComboBox<ClassInfo> classCombo;
    @FXML private Button loadBtn;
    @FXML private TableView<StudentScoreEntry> table;
    @FXML private TableColumn<StudentScoreEntry, String> colStudentNo;
    @FXML private TableColumn<StudentScoreEntry, String> colName;
    @FXML private TableColumn<StudentScoreEntry, TextField> colRegular;
    @FXML private TableColumn<StudentScoreEntry, TextField> colExam;
    @FXML private TableColumn<StudentScoreEntry, Void> colAction;

    private final ObservableList<StudentScoreEntry> tableData = FXCollections.observableArrayList();

    public static class StudentScoreEntry {
        private final Student student;
        private final TextField regularField = new TextField();
        private final TextField examField = new TextField();

        public StudentScoreEntry(Student student) {
            this.student = student;
            regularField.setPrefWidth(100);
            examField.setPrefWidth(100);
        }

        public Student getStudent() { return student; }
        public TextField getRegularField() { return regularField; }
        public TextField getExamField() { return examField; }
    }

    @FXML
    public void initialize() {
        loadExams();
        loadCourses();
        loadClasses();

        colStudentNo.setCellValueFactory(data ->
            new javafx.beans.binding.StringBinding() {
                { bind(data.getValue().getRegularField().textProperty()); }
                @Override protected String computeValue() { return data.getValue().getStudent().getStudentNo(); }
            });
        colName.setCellValueFactory(data ->
            new javafx.beans.binding.StringBinding() {
                { bind(data.getValue().getRegularField().textProperty()); }
                @Override protected String computeValue() { return data.getValue().getStudent().getName(); }
            });
        colRegular.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyObjectWrapper<>(data.getValue().getRegularField()));
        colExam.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyObjectWrapper<>(data.getValue().getExamField()));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("提交");
            {
                btn.getStyleClass().addAll("btn-primary", "btn-sm");
                btn.setOnAction(e -> handleSubmit(getTableView().getItems().get(getIndex())));
                HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn.getParent());
            }
        });

        table.setItems(tableData);
    }

    private void loadExams() {
        Task<List<Exam>> task = new Task<>() {
            @Override protected List<Exam> call() throws Exception { return ExamService.getPage(1, 100, null).getRecords(); }
        };
        task.setOnSucceeded(e -> examCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        new Thread(task).start();
    }

    private void loadCourses() {
        Task<List<Course>> task = new Task<>() {
            @Override protected List<Course> call() throws Exception { return CourseService.getAll(); }
        };
        task.setOnSucceeded(e -> courseCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        new Thread(task).start();
    }

    private void loadClasses() {
        Task<List<ClassInfo>> task = new Task<>() {
            @Override protected List<ClassInfo> call() throws Exception { return ClassService.getAll(); }
        };
        task.setOnSucceeded(e -> classCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        new Thread(task).start();
    }

    @FXML
    private void handleLoadStudents() {
        ClassInfo selectedClass = classCombo.getValue();
        if (selectedClass == null) {
            CrudHelper.showAlert("请选择班级");
            return;
        }
        Task<List<Student>> task = new Task<>() {
            @Override protected List<Student> call() throws Exception {
                return StudentService.getPage(1, 200, null, selectedClass.getId()).getRecords();
            }
        };
        task.setOnSucceeded(e -> {
            tableData.clear();
            task.getValue().forEach(s -> tableData.add(new StudentScoreEntry(s)));
        });
        task.setOnFailed(e -> CrudHelper.showError("加载学生失败"));
        new Thread(task).start();
    }

    private void handleSubmit(StudentScoreEntry entry) {
        Exam exam = examCombo.getValue();
        Course course = courseCombo.getValue();
        if (exam == null || course == null) {
            CrudHelper.showAlert("请先选择考试和课程");
            return;
        }
        try {
            double regular = Double.parseDouble(entry.getRegularField().getText());
            double examScore = Double.parseDouble(entry.getExamField().getText());
            Task<Void> task = new Task<>() {
                @Override protected Void call() throws Exception {
                    ScoreService.createScore(entry.getStudent().getId(), exam.getId(), course.getId(), regular, examScore);
                    return null;
                }
            };
            task.setOnSucceeded(e -> CrudHelper.showAlert("提交成功"));
            task.setOnFailed(e -> CrudHelper.showError("提交失败"));
            new Thread(task).start();
        } catch (NumberFormatException ex) {
            CrudHelper.showAlert("请输入有效的分数");
        }
    }
}
