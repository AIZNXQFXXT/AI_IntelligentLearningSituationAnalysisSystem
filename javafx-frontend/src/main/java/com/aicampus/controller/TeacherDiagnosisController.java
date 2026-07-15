package com.aicampus.controller;

import com.aicampus.model.AiDiagnosis;
import com.aicampus.model.Student;
import com.aicampus.service.DiagnosisService;
import com.aicampus.service.StudentService;
import com.aicampus.util.AppExecutors;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;

import java.util.Arrays;
import java.util.List;

public class TeacherDiagnosisController {

    @FXML private TableView<AiDiagnosis> table;
    @FXML private TableColumn<AiDiagnosis, Integer> colId;
    @FXML private TableColumn<AiDiagnosis, String> colSemester;
    @FXML private TableColumn<AiDiagnosis, String> colRiskLevel;
    @FXML private TableColumn<AiDiagnosis, String> colDiagnosis;
    @FXML private TableColumn<AiDiagnosis, String> colAiModel;
    @FXML private TableColumn<AiDiagnosis, Integer> colTokens;
    @FXML private TableColumn<AiDiagnosis, String> colCreatedAt;
    @FXML private TableColumn<AiDiagnosis, Void> colAction;
    @FXML private ComboBox<String> semesterCombo;
    @FXML private ComboBox<Student> studentCombo;

    private final ObservableList<AiDiagnosis> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        semesterCombo.setItems(FXCollections.observableArrayList(Arrays.asList("2025-1", "2024-2", "2024-1")));
        semesterCombo.getSelectionModel().selectFirst();
        semesterCombo.setOnAction(e -> loadData());

        loadStudents();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colRiskLevel.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosisText"));
        colAiModel.setCellValueFactory(new PropertyValueFactory<>("aiModel"));
        colTokens.setCellValueFactory(new PropertyValueFactory<>("tokensUsed"));
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

    private void loadStudents() {
        Task<List<Student>> task = new Task<>() {
            @Override
            protected List<Student> call() throws Exception {
                return StudentService.getPage(1, 500).getRecords();
            }
        };
        task.setOnSucceeded(e -> studentCombo.setItems(FXCollections.observableArrayList(task.getValue())));
        AppExecutors.submit(task::run);
    }

    private void loadData() {
        String semester = semesterCombo.getValue();
        Task<List<AiDiagnosis>> task = new Task<>() {
            @Override
            protected List<AiDiagnosis> call() throws Exception {
                return DiagnosisService.getPage(1, 50, semester).getRecords();
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
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("诊断报告详情");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new javafx.geometry.Insets(20));

        int row = 0;
        grid.add(new Label("诊断文本:"), 0, row);
        TextArea t = new TextArea(d.getDiagnosisText());
        t.setEditable(false);
        t.setPrefHeight(80);
        grid.add(t, 1, row++);

        grid.add(new Label("优势:"), 0, row);
        TextArea s = new TextArea(d.getStrengths());
        s.setEditable(false);
        s.setPrefHeight(60);
        grid.add(s, 1, row++);

        grid.add(new Label("劣势:"), 0, row);
        TextArea w = new TextArea(d.getWeaknesses());
        w.setEditable(false);
        w.setPrefHeight(60);
        grid.add(w, 1, row++);

        grid.add(new Label("趋势分析:"), 0, row);
        TextArea tr = new TextArea(d.getTrendAnalysis());
        tr.setEditable(false);
        tr.setPrefHeight(60);
        grid.add(tr, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(700);
        dialog.showAndWait();
    }
}
