package com.aicampus.controller;

import com.aicampus.model.AiDiagnosis;
import com.aicampus.service.DiagnosisService;
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

import java.util.List;

public class StudentDiagnosisController {

    @FXML private TableView<AiDiagnosis> table;
    @FXML private TableColumn<AiDiagnosis, Integer> colId;
    @FXML private TableColumn<AiDiagnosis, String> colSemester;
    @FXML private TableColumn<AiDiagnosis, String> colRiskLevel;
    @FXML private TableColumn<AiDiagnosis, String> colDiagnosis;
    @FXML private TableColumn<AiDiagnosis, String> colCreatedAt;
    @FXML private TableColumn<AiDiagnosis, Void> colAction;
    @FXML private Label emptyLabel;

    private final ObservableList<AiDiagnosis> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colRiskLevel.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        colDiagnosis.setCellValueFactory(new PropertyValueFactory<>("diagnosisText"));
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
                        case "HIGH" -> label.getStyleClass().add("tag-danger");
                        case "MEDIUM" -> label.getStyleClass().add("tag-warning");
                        default -> label.getStyleClass().add("tag-success");
                    }
                    setGraphic(label);
                }
            }
        });

        colDiagnosis.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item);
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
                return DiagnosisService.getPage(1, 50).getRecords();
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
        task.setOnFailed(e -> CrudHelper.showError("加载失败"));
        new Thread(task).start();
    }

    private void showDetail(AiDiagnosis d) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("诊断报告详情");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new javafx.geometry.Insets(20));

        grid.add(new Label("诊断文本:"), 0, 0);
        TextArea diagnosisArea = new TextArea(d.getDiagnosisText());
        diagnosisArea.setEditable(false);
        diagnosisArea.setPrefHeight(80);
        grid.add(diagnosisArea, 1, 0);

        grid.add(new Label("优势:"), 0, 1);
        TextArea strengthsArea = new TextArea(d.getStrengths());
        strengthsArea.setEditable(false);
        strengthsArea.setPrefHeight(60);
        grid.add(strengthsArea, 1, 1);

        grid.add(new Label("劣势:"), 0, 2);
        TextArea weaknessesArea = new TextArea(d.getWeaknesses());
        weaknessesArea.setEditable(false);
        weaknessesArea.setPrefHeight(60);
        grid.add(weaknessesArea, 1, 2);

        grid.add(new Label("趋势分析:"), 0, 3);
        TextArea trendArea = new TextArea(d.getTrendAnalysis());
        trendArea.setEditable(false);
        trendArea.setPrefHeight(60);
        grid.add(trendArea, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(700);
        dialog.showAndWait();
    }
}
