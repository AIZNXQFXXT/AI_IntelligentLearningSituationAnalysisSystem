package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.LogEntry;
import com.campus.client.model.PageResult;
import com.campus.client.service.LogService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class LogController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private TableView<LogEntry> operationLogTable;
    @FXML private TableColumn<LogEntry, Long> colLogId;
    @FXML private TableColumn<LogEntry, String> colUsername;
    @FXML private TableColumn<LogEntry, String> colOperation;
    @FXML private TableColumn<LogEntry, String> colMethod;
    @FXML private TableColumn<LogEntry, String> colIp;
    @FXML private TableColumn<LogEntry, String> colStatus;
    @FXML private TableColumn<LogEntry, String> colCreateTime;
    @FXML private TableView<LogEntry> aiCallLogTable;
    @FXML private TableColumn<LogEntry, Long> colAiLogId;
    @FXML private TableColumn<LogEntry, String> colAiUsername;
    @FXML private TableColumn<LogEntry, String> colAiOperation;
    @FXML private TableColumn<LogEntry, String> colAiMethod;
    @FXML private TableColumn<LogEntry, String> colAiIp;
    @FXML private TableColumn<LogEntry, String> colAiStatus;
    @FXML private TableColumn<LogEntry, String> colAiCreateTime;
    @FXML private Label opPageInfo;
    @FXML private Label aiPageInfo;

    private final LogService logService = new LogService();
    private int opPage = 0, opTotalPages = 1;
    private int aiPage = 0, aiTotalPages = 1;
    private final ObservableList<LogEntry> opData = FXCollections.observableArrayList();
    private final ObservableList<LogEntry> aiData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupOpTable();
        setupAiTable();
        loadOpLogs();
        loadAiLogs();
    }

    private void setupOpTable() {
        colLogId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colOperation.setCellValueFactory(new PropertyValueFactory<>("operation"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        colIp.setCellValueFactory(new PropertyValueFactory<>("ip"));
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus() != null && cellData.getValue().getStatus() == 200 ? "成功" : "失败"));
        colCreateTime.setCellValueFactory(new PropertyValueFactory<>("createTime"));
        operationLogTable.setItems(opData);
    }

    private void setupAiTable() {
        colAiLogId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAiUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAiOperation.setCellValueFactory(new PropertyValueFactory<>("operation"));
        colAiMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        colAiIp.setCellValueFactory(new PropertyValueFactory<>("ip"));
        colAiStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus() != null && cellData.getValue().getStatus() == 200 ? "成功" : "失败"));
        colAiCreateTime.setCellValueFactory(new PropertyValueFactory<>("createTime"));
        aiCallLogTable.setItems(aiData);
    }

    private void loadOpLogs() {
        Task<ApiResult<PageResult<LogEntry>>> task = new Task<>() {
            @Override protected ApiResult<PageResult<LogEntry>> call() throws Exception { return logService.getOperationLogs(opPage, 20); }
        };
        task.setOnSucceeded(e -> {
            ApiResult<PageResult<LogEntry>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) {
                opData.setAll(result.getData().getContent());
                opTotalPages = Math.max(1, result.getData().getTotalPages());
                opPageInfo.setText("第 " + (opPage + 1) + " 页 / 共 " + opTotalPages + " 页");
            }
        });
        new Thread(task).start();
    }

    private void loadAiLogs() {
        Task<ApiResult<PageResult<LogEntry>>> task = new Task<>() {
            @Override protected ApiResult<PageResult<LogEntry>> call() throws Exception { return logService.getAiCallLogs(aiPage, 20); }
        };
        task.setOnSucceeded(e -> {
            ApiResult<PageResult<LogEntry>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) {
                aiData.setAll(result.getData().getContent());
                aiTotalPages = Math.max(1, result.getData().getTotalPages());
                aiPageInfo.setText("第 " + (aiPage + 1) + " 页 / 共 " + aiTotalPages + " 页");
            }
        });
        new Thread(task).start();
    }

    @FXML private void handlePrevOpPage() { if (opPage > 0) { opPage--; loadOpLogs(); } }
    @FXML private void handleNextOpPage() { if (opPage < opTotalPages - 1) { opPage++; loadOpLogs(); } }
    @FXML private void handlePrevAiPage() { if (aiPage > 0) { aiPage--; loadAiLogs(); } }
    @FXML private void handleNextAiPage() { if (aiPage < aiTotalPages - 1) { aiPage++; loadAiLogs(); } }
}
