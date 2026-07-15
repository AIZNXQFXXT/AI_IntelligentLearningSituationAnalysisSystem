package com.aicampus.controller;

import com.aicampus.model.Score;
import com.aicampus.service.ScoreService;
import com.aicampus.util.AppExecutors;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Arrays;
import java.util.List;

public class StudentScoresController {

    @FXML private TableView<Score> table;
    @FXML private TableColumn<Score, Integer> colCourseId;
    @FXML private TableColumn<Score, Double> colRegular;
    @FXML private TableColumn<Score, Double> colExam;
    @FXML private TableColumn<Score, Double> colFinal;
    @FXML private TableColumn<Score, Integer> colRankClass;
    @FXML private TableColumn<Score, Integer> colRankGrade;
    @FXML private TableColumn<Score, String> colAudit;
    @FXML private ComboBox<String> semesterFilter;
    @FXML private Label emptyLabel;

    private final ObservableList<Score> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        semesterFilter.setItems(FXCollections.observableArrayList(
            Arrays.asList("2025-1", "2024-2", "2024-1")
        ));
        semesterFilter.getSelectionModel().selectFirst();
        semesterFilter.setOnAction(e -> loadData());

        colCourseId.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        colRegular.setCellValueFactory(new PropertyValueFactory<>("regularScore"));
        colExam.setCellValueFactory(new PropertyValueFactory<>("examScore"));
        colFinal.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
        colRankClass.setCellValueFactory(new PropertyValueFactory<>("rankClass"));
        colRankGrade.setCellValueFactory(new PropertyValueFactory<>("rankGrade"));
        colAudit.setCellValueFactory(new PropertyValueFactory<>("auditStatus"));

        colFinal.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(String.format("%.1f", item));
                    label.setStyle("-fx-font-weight: bold;");
                    setGraphic(label);
                }
            }
        });

        colAudit.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label("APPROVED".equals(item) ? "已审核" : "待审核");
                    label.getStyleClass().add("APPROVED".equals(item) ? "tag-success" : "tag-info");
                    setGraphic(label);
                }
            }
        });

        table.setItems(tableData);
        loadData();
    }

    private void loadData() {
        Task<List<Score>> task = new Task<>() {
            @Override
            protected List<Score> call() throws Exception {
                return ScoreService.getScorePage(1, 200).getRecords();
            }
        };
        task.setOnSucceeded(e -> {
            List<Score> scores = task.getValue();
            tableData.clear();
            tableData.addAll(scores);
            emptyLabel.setVisible(scores.isEmpty());
            emptyLabel.setManaged(scores.isEmpty());
            table.setVisible(!scores.isEmpty());
            table.setManaged(!scores.isEmpty());
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }
}
