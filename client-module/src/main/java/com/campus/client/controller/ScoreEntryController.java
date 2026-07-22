package com.campus.client.controller;

import com.campus.client.model.Score;
import com.campus.client.model.ClassInfo;
import com.campus.client.service.ScoreService;
import com.campus.client.service.ClassService;
import com.campus.client.service.CourseService;
import com.campus.client.service.ExamService;
import com.campus.client.service.StudentService;
import com.campus.common.dto.CourseDTO;
import com.campus.common.dto.ExamDTO;
import com.campus.client.model.Student;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreEntryController {

    @FXML private ComboBox<ExamDTO> examCombo;
    @FXML private ComboBox<CourseDTO> courseCombo;
    @FXML private ComboBox<ClassInfo> classCombo;
    @FXML private Button loadBtn;
    @FXML private TableView<StudentScoreEntry> table;
    @FXML private TableColumn<StudentScoreEntry, String> colStudentNo;
    @FXML private TableColumn<StudentScoreEntry, String> colName;
    @FXML private TableColumn<StudentScoreEntry, TextField> colRegular;
    @FXML private TableColumn<StudentScoreEntry, TextField> colExam;
    @FXML private TableColumn<StudentScoreEntry, TextField> colFinal;
    @FXML private TableColumn<StudentScoreEntry, String> colStatus;
    @FXML private TableColumn<StudentScoreEntry, Void> colAction;

    private final ObservableList<StudentScoreEntry> tableData = FXCollections.observableArrayList();

    public static class StudentScoreEntry {
        private final Student student;
        private final TextField regularField = new TextField();
        private final TextField examField = new TextField();
        private final TextField finalField = new TextField();
        private boolean archived = false;
        private int scoreId = 0;
        private String auditStatus;
        private final SimpleStringProperty statusProperty = new SimpleStringProperty("未录入");

        public StudentScoreEntry(Student student) {
            this.student = student;
            regularField.setPrefWidth(100);
            examField.setPrefWidth(100);
            finalField.setPrefWidth(100);
        }

        public Student getStudent() { return student; }
        public TextField getRegularField() { return regularField; }
        public TextField getExamField() { return examField; }
        public TextField getFinalField() { return finalField; }
        public boolean isArchived() { return archived; }
        public void setArchived(boolean archived) { this.archived = archived; updateStatus(); }
        public int getScoreId() { return scoreId; }
        public void setScoreId(int scoreId) { this.scoreId = scoreId; updateStatus(); }
        public String getAuditStatus() { return auditStatus; }
        public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; updateStatus(); }
        public String getStatus() { return statusProperty.get(); }
        public SimpleStringProperty statusProperty() { return statusProperty; }
        public void updateStatus() {
            if (archived) statusProperty.set("已归档");
            else if ("SUBMITTED".equals(auditStatus)) statusProperty.set("已提交");
            else if (scoreId > 0) statusProperty.set("未归档");
            else statusProperty.set("未录入");
        }
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
        colFinal.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyObjectWrapper<>(data.getValue().getFinalField()));
        colStatus.setCellValueFactory(data -> data.getValue().statusProperty());

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn1 = new Button();
            private final Button btn2 = new Button();
            private final HBox box = new HBox(6, btn1, btn2);
            {
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                StudentScoreEntry entry = getTableView().getItems().get(getIndex());
                btn1.getStyleClass().removeAll("btn-primary", "btn-warning", "btn-success", "btn-default");
                btn2.getStyleClass().removeAll("btn-primary", "btn-warning", "btn-success", "btn-default");
                btn1.setDisable(false);
                btn2.setDisable(false);
                btn2.setVisible(true);
                btn2.setManaged(true);

                if (entry.isArchived()) {
                    btn1.setText("已归档");
                    btn1.getStyleClass().add("btn-success");
                    btn1.setDisable(true);
                    btn1.setOnAction(null);
                    btn2.setVisible(false);
                    btn2.setManaged(false);
                } else if ("SUBMITTED".equals(entry.getAuditStatus())) {
                    btn1.setText("已提交");
                    btn1.getStyleClass().add("btn-default");
                    btn1.setDisable(true);
                    btn1.setOnAction(null);
                    btn2.setText("归档");
                    btn2.getStyleClass().add("btn-warning");
                    btn2.setOnAction(e -> handleArchive(entry));
                } else if (entry.getScoreId() > 0) {
                    btn1.setText("修改");
                    btn1.getStyleClass().add("btn-primary");
                    btn1.setOnAction(e -> handleModify(entry));
                    btn2.setText("归档");
                    btn2.getStyleClass().add("btn-warning");
                    btn2.setOnAction(e -> handleArchive(entry));
                } else {
                    btn1.setText("提交");
                    btn1.getStyleClass().add("btn-primary");
                    btn1.setOnAction(e -> handleSubmit(entry));
                    btn2.setVisible(false);
                    btn2.setManaged(false);
                }
                setGraphic(box);
            }
        });

        table.setItems(tableData);
    }

    private void loadExams() {
        Task<List<ExamDTO>> task = new Task<>() {
            @Override protected List<ExamDTO> call() throws Exception { return ExamService.getPage(1, 100, null).getRecords(); }
        };
        task.setOnSucceeded(e -> examCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> CrudHelper.showError("加载考试列表失败"));
        AppExecutors.submit(task::run);
    }

    private void loadCourses() {
        Task<List<CourseDTO>> task = new Task<>() {
            @Override protected List<CourseDTO> call() throws Exception { return CourseService.getAll(); }
        };
        task.setOnSucceeded(e -> courseCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> CrudHelper.showError("加载课程列表失败"));
        AppExecutors.submit(task::run);
    }

    private void loadClasses() {
        Task<List<ClassInfo>> task = new Task<>() {
            @Override protected List<ClassInfo> call() throws Exception { return ClassService.getAll(); }
        };
        task.setOnSucceeded(e -> classCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> CrudHelper.showError("加载班级列表失败"));
        AppExecutors.submit(task::run);
    }

    @FXML
    private void handleLoadStudents() {
        ExamDTO exam = examCombo.getValue();
        CourseDTO course = courseCombo.getValue();
        ClassInfo selectedClass = classCombo.getValue();
        if (selectedClass == null || exam == null || course == null) {
            CrudHelper.showAlert("请选择考试、课程和班级");
            return;
        }

        Task<List<StudentScoreEntry>> task = new Task<>() {
            @Override protected List<StudentScoreEntry> call() throws Exception {
                List<Student> students = StudentService.getPage(1, 200, null, selectedClass.getId()).getRecords();
                List<Score> existingScores = ScoreService.getScoresByExamAndCourse(exam.getId().intValue(), course.getId().intValue());
                Map<Integer, Score> scoreMap = new HashMap<>();
                for (Score s : existingScores) scoreMap.put(s.getStudentId(), s);

                List<StudentScoreEntry> entries = new ArrayList<>();
                for (Student student : students) {
                    StudentScoreEntry entry = new StudentScoreEntry(student);
                    Score existing = scoreMap.get(student.getId());
                    if (existing != null) {
                        entry.setScoreId(existing.getId());
                        entry.setAuditStatus(existing.getAuditStatus());
                        entry.getRegularField().setText(String.valueOf(existing.getRegularScore()));
                        entry.getExamField().setText(String.valueOf(existing.getExamScore()));
                        entry.getFinalField().setText(String.valueOf(existing.getFinalScore()));
                        if ("ARCHIVED".equals(existing.getAuditStatus())) {
                            entry.setArchived(true);
                            entry.getRegularField().setEditable(false);
                            entry.getExamField().setEditable(false);
                            entry.getFinalField().setEditable(false);
                        } else if ("SUBMITTED".equals(existing.getAuditStatus())) {
                            entry.getRegularField().setEditable(false);
                            entry.getExamField().setEditable(false);
                            entry.getFinalField().setEditable(false);
                        }
                    }
                    entry.updateStatus();
                    entries.add(entry);
                }
                return entries;
            }
        };
        task.setOnSucceeded(e -> { tableData.clear(); tableData.addAll(task.getValue()); });
        task.setOnFailed(e -> CrudHelper.showError("加载数据失败"));
        AppExecutors.submit(task::run);
    }

    private void handleSubmit(StudentScoreEntry entry) {
        ExamDTO exam = examCombo.getValue();
        CourseDTO course = courseCombo.getValue();
        if (exam == null || course == null) {
            CrudHelper.showAlert("请先选择考试和课程");
            return;
        }
        try {
            double regular = Double.parseDouble(entry.getRegularField().getText());
            double examScore = Double.parseDouble(entry.getExamField().getText());
            double finalScore = Double.parseDouble(entry.getFinalField().getText());
            Task<Score> task = new Task<>() {
                @Override protected Score call() throws Exception {
                    return ScoreService.createScore(entry.getStudent().getId(), exam.getId().intValue(), course.getId().intValue(), regular, examScore, finalScore);
                }
            };
            task.setOnSucceeded(e -> {
                Score saved = task.getValue();
                if (saved != null) {
                    entry.setScoreId(saved.getId());
                }
                table.refresh();
            });
            task.setOnFailed(e -> CrudHelper.showError("提交失败"));
            AppExecutors.submit(task::run);
        } catch (NumberFormatException ex) {
            CrudHelper.showAlert("请输入有效的分数");
        }
    }

    private void handleModify(StudentScoreEntry entry) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("修改成绩");
        dialog.setHeaderText("请输入修改原因");
        dialog.getDialogPane().setPrefWidth(400);
        dialog.showAndWait().ifPresent(reason -> {
            if (reason.isBlank()) {
                CrudHelper.showAlert("修改原因不能为空");
                return;
            }
            try {
                double regular = Double.parseDouble(entry.getRegularField().getText());
                double examScore = Double.parseDouble(entry.getExamField().getText());
                double finalScore = Double.parseDouble(entry.getFinalField().getText());
                Task<Score> task = new Task<>() {
                    @Override protected Score call() throws Exception {
                        return ScoreService.updateScore(entry.getScoreId(), regular, examScore, finalScore, reason);
                    }
                };
                task.setOnSucceeded(e -> {
                    entry.setAuditStatus("SUBMITTED");
                    entry.getRegularField().setEditable(false);
                    entry.getExamField().setEditable(false);
                    entry.getFinalField().setEditable(false);
                    table.refresh();
                    CrudHelper.showAlert("修改成功");
                });
                task.setOnFailed(e -> CrudHelper.showError("修改失败"));
                AppExecutors.submit(task::run);
            } catch (NumberFormatException ex) {
                CrudHelper.showAlert("请输入有效的分数");
            }
        });
    }

    private void handleArchive(StudentScoreEntry entry) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                return ScoreService.archiveScore(entry.getScoreId());
            }
        };
        task.setOnSucceeded(e -> {
            entry.setArchived(true);
            table.refresh();
            CrudHelper.showAlert("归档成功");
        });
        task.setOnFailed(e -> CrudHelper.showError("归档失败"));
        AppExecutors.submit(task::run);
    }
}
