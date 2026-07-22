package com.aicampus.controller;

import com.aicampus.model.AiDiagnosis;
import com.aicampus.model.DiagnosisReport;
import com.aicampus.service.DiagnosisService;
import com.aicampus.util.AppExecutors;
import com.aicampus.util.CrudHelper;
import com.aicampus.util.JsonFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;

import java.util.List;

public class StudentDiagnosisController {

    @FXML private TableView<AiDiagnosis> table;
    @FXML private TableColumn<AiDiagnosis, String> colSemester;
    @FXML private TableColumn<AiDiagnosis, String> colRiskLevel;
    @FXML private TableColumn<AiDiagnosis, String> colDiagnosis;
    @FXML private TableColumn<AiDiagnosis, String> colCreatedAt;
    @FXML private TableColumn<AiDiagnosis, Void> colAction;
    @FXML private Label emptyLabel;

    private final ObservableList<AiDiagnosis> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colRiskLevel.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colRiskLevel.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
                    switch (item) {
                        case "HIGH": label.getStyleClass().add("tag-danger"); break;
                        case "MEDIUM": label.getStyleClass().add("tag-warning"); break;
                        default: label.getStyleClass().add("tag-success"); break;
                    }
                    setGraphic(label);
                }
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

        table.setItems(tableData);
        loadData();
    }

    private void loadData() {
        Task<List<AiDiagnosis>> task = new Task<>() {
            @Override
            protected List<AiDiagnosis> call() throws Exception {
                return DiagnosisService.getMyPage(1, 50).getRecords();
            }
        };
        task.setOnSucceeded(e -> {
            List<AiDiagnosis> list = task.getValue();
            tableData.clear();
            tableData.addAll(list);
            emptyLabel.setVisible(list.isEmpty());
            emptyLabel.setManaged(list.isEmpty());
            table.setVisible(!list.isEmpty());
            table.setManaged(!list.isEmpty());
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
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
        TextArea overallArea = new TextArea(report.getOverall() != null ? report.getOverall() : d.getDiagnosisText());
        overallArea.setEditable(false);
        overallArea.setPrefHeight(80);
        grid.add(overallArea, 1, row++);

        grid.add(new Label("优势:"), 0, row);
        TextArea strengthsArea = new TextArea(JsonFormatter.formatKeyValueList(report.getStrengths(), "科目", "描述"));
        strengthsArea.setEditable(false);
        strengthsArea.setPrefHeight(60);
        grid.add(strengthsArea, 1, row++);

        grid.add(new Label("劣势:"), 0, row);
        TextArea weaknessesArea = new TextArea(JsonFormatter.formatKeyValueList(report.getWeaknesses(), "科目", "描述"));
        weaknessesArea.setEditable(false);
        weaknessesArea.setPrefHeight(60);
        grid.add(weaknessesArea, 1, row++);

        grid.add(new Label("趋势分析:"), 0, row);
        TextArea trendArea = new TextArea(report.getTrend() != null ? report.getTrend() : d.getTrend());
        trendArea.setEditable(false);
        trendArea.setPrefHeight(60);
        grid.add(trendArea, 1, row++);

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
