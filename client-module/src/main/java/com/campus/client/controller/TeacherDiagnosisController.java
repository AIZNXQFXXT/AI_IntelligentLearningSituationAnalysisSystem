package com.campus.client.controller;

import com.campus.client.model.AiDiagnosis;
import com.campus.client.model.DiagnosisReport;
import com.campus.client.model.Student;
import com.campus.client.service.DiagnosisService;
import com.campus.client.service.SemesterService;
import com.campus.client.service.StudentService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import com.campus.client.util.JsonFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

public class TeacherDiagnosisController {

    @FXML private TableView<AiDiagnosis> table;
    @FXML private TableColumn<AiDiagnosis, String> colStudent;
    @FXML private TableColumn<AiDiagnosis, String> colSemester;
    @FXML private TableColumn<AiDiagnosis, String> colRiskLevel;
    @FXML private TableColumn<AiDiagnosis, String> colDiagnosis;
    @FXML private TableColumn<AiDiagnosis, String> colCreatedAt;
    @FXML private TableColumn<AiDiagnosis, Void> colAction;
    @FXML private ComboBox<String> semesterCombo;
    @FXML private ComboBox<Student> studentCombo;
    @FXML private TextField searchField;

    private final ObservableList<AiDiagnosis> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        semesterCombo.setOnAction(e -> loadData());
        loadSemesters();

        loadStudents();

        colStudent.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStudentName() != null
                        ? data.getValue().getStudentName() + " (" + (data.getValue().getStudentNo() != null ? data.getValue().getStudentNo() : "") + ")"
                        : ""));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colRiskLevel.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colRiskLevel.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label label = new Label(item);
                switch (item) {
                    case "HIGH": label.getStyleClass().add("tag-danger"); break;
                    case "MEDIUM": label.getStyleClass().add("tag-warning"); break;
                    default: label.getStyleClass().add("tag-success"); break;
                }
                setGraphic(label);
            }
        });

        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosisText"));
        colDiagnosis.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    String preview = item;
                    DiagnosisReport report = JsonFormatter.parseDiagnosisReport(item);
                    if (report.getOverall() != null && !report.getOverall().isBlank()) {
                        preview = report.getOverall();
                    }
                    if (preview.length() > 60) preview = preview.substring(0, 60) + "...";
                    Label label = new Label(preview);
                    label.setWrapText(true);
                    setGraphic(label);
                }
            }
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("查看详情");
            {
                btn.getStyleClass().addAll("btn-primary", "btn-sm");
                btn.setOnAction(e -> showDetail(getTableView().getItems().get(getIndex())));
                HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn.getParent());
            }
        });

        studentCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getStudentNo() + ")");
            }
        });
        studentCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Student item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getStudentNo() + ")");
            }
        });

        table.setItems(tableData);
        loadData();
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

    private void loadStudents() {
        Task<List<Student>> task = new Task<>() {
            @Override
            protected List<Student> call() throws Exception {
                return StudentService.getPage(1, 500).getRecords();
            }
        };
        task.setOnSucceeded(e -> studentCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        task.setOnFailed(e -> CrudHelper.showError("加载学生列表失败"));
        AppExecutors.submit(task::run);
    }

    @FXML
    private void handleSearch() {
        loadData();
    }

    private void loadData() {
        String semester = semesterCombo.getValue();
        final String keyword = searchField != null && searchField.getText() != null
                ? searchField.getText().trim() : "";
        Task<List<AiDiagnosis>> task = new Task<>() {
            @Override
            protected List<AiDiagnosis> call() throws Exception {
                return DiagnosisService.getPage(1, 50, semester, keyword).getRecords();
            }
        };
        task.setOnSucceeded(e -> {
            tableData.clear();
            tableData.addAll(task.getValue());
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }

    @FXML
    private void handleGenerate() {
        Student student = studentCombo.getValue();
        String semester = semesterCombo.getValue();
        if (student == null) {
            CrudHelper.showAlert("请选择学生");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                DiagnosisService.generate(student.getId(), semester);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            CrudHelper.showAlert("诊断生成成功");
            loadData();
        });
        task.setOnFailed(e -> CrudHelper.showError("生成失败"));
        AppExecutors.submit(task::run);
    }

    private void showDetail(AiDiagnosis d) {
        DiagnosisReport report = JsonFormatter.parseDiagnosisReport(d.getDiagnosisText());

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("诊断报告详情");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new javafx.geometry.Insets(20));

        int row = 0;
        grid.add(new Label("总体评价:"), 0, row);
        TextArea t = new TextArea(report.getOverall() != null ? report.getOverall() : d.getDiagnosisText());
        t.setEditable(false);
        t.setPrefHeight(80);
        grid.add(t, 1, row++);

        grid.add(new Label("优势:"), 0, row);
        TextArea s = new TextArea(JsonFormatter.formatKeyValueList(report.getStrengths(), "科目", "描述"));
        s.setEditable(false);
        s.setPrefHeight(60);
        grid.add(s, 1, row++);

        grid.add(new Label("劣势:"), 0, row);
        TextArea w = new TextArea(JsonFormatter.formatKeyValueList(report.getWeaknesses(), "科目", "描述"));
        w.setEditable(false);
        w.setPrefHeight(60);
        grid.add(w, 1, row++);

        grid.add(new Label("趋势分析:"), 0, row);
        TextArea tr = new TextArea(report.getTrend() != null ? report.getTrend() : d.getTrend());
        tr.setEditable(false);
        tr.setPrefHeight(60);
        grid.add(tr, 1, row++);

        grid.add(new Label("学习建议:"), 0, row);
        TextArea suggestionsArea = new TextArea(JsonFormatter.formatNumberedList(report.getSuggestions()));
        suggestionsArea.setEditable(false);
        suggestionsArea.setPrefHeight(80);
        grid.add(suggestionsArea, 1, row++);

        grid.add(new Label("风险等级:"), 0, row);
        Label riskLabel = new Label(JsonFormatter.formatRiskLevel(report.getRiskLevel()));
        riskLabel.getStyleClass().add("tag-" + switch (report.getRiskLevel() != null ? report.getRiskLevel().toUpperCase() : "") {
            case "HIGH" -> "danger";
            case "MEDIUM" -> "warning";
            default -> "success";
        });
        grid.add(riskLabel, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(700);
        dialog.showAndWait();
    }
}
