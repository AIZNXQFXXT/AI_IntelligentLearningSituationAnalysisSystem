package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.service.ReportService;
import com.campus.client.service.StatsService;
import com.campus.client.util.AlertHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class TeacherDashboardController implements Initializable {

    @FXML private Label myClassCount;
    @FXML private Label myStudentCount;
    @FXML private Label myPendingWarnings;
    @FXML private TableView<Map<String, Object>> classOverviewTable;
    @FXML private TableColumn<Map<String, Object>, String> colClassName;
    @FXML private TableColumn<Map<String, Object>, String> colAvgScore;
    @FXML private TableColumn<Map<String, Object>, String> colMaxScore;
    @FXML private TableColumn<Map<String, Object>, String> colMinScore;
    @FXML private TableColumn<Map<String, Object>, String> colPassRate;

    private final StatsService statsService = new StatsService();
    private final ReportService reportService = new ReportService();
    private final ObservableList<Map<String, Object>> overviewData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colClassName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(cellData.getValue().getOrDefault("className", ""))));
        colAvgScore.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(cellData.getValue().getOrDefault("avgScore", ""))));
        colMaxScore.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(cellData.getValue().getOrDefault("maxScore", ""))));
        colMinScore.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(cellData.getValue().getOrDefault("minScore", ""))));
        colPassRate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(cellData.getValue().getOrDefault("passRate", ""))));
        classOverviewTable.setItems(overviewData);
    }

    private void loadData() {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                try {
                    ApiResult<Map<String, Object>> risk = statsService.getRiskSummary();
                    if (risk.isSuccess() && risk.getData() != null) {
                        Map<String, Object> data = risk.getData();
                        Platform.runLater(() -> {
                            setLabel(myPendingWarnings, data, "pendingWarnings");
                        });
                    }
                } catch (Exception e) { }
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void handleExportScores() { exportReport("score-table", "成绩表"); }

    @FXML
    private void handleExportComments() { exportReport("comments", "评语"); }

    @FXML
    private void handleExportRiskList() { exportReport("risk-list", "预警列表"); }

    private void exportReport(String type, String name) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                byte[] data = reportService.exportScoreTable(null, null);
                File file = new File(name + "_export.xlsx");
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(data);
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> AlertHelper.showInfo("成功", name + "导出成功"));
        task.setOnFailed(e -> AlertHelper.showError("失败", "导出失败"));
        new Thread(task).start();
    }

    private void setLabel(Label label, Map<String, Object> data, String key) {
        if (data.containsKey(key) && data.get(key) != null) label.setText(String.valueOf(data.get(key)));
    }
}
