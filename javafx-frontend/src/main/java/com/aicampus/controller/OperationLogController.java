package com.aicampus.controller;

import com.aicampus.model.OperationLog;
import com.aicampus.model.PageResult;
import com.aicampus.service.OperationLogService;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class OperationLogController {

    @FXML private TextField usernameField;
    @FXML private TextField operationField;
    @FXML private TextField startDateField;
    @FXML private TextField endDateField;
    @FXML private TableView<OperationLog> table;
    @FXML private TableColumn<OperationLog, Integer> colId;
    @FXML private TableColumn<OperationLog, String> colUsername;
    @FXML private TableColumn<OperationLog, String> colOperation;
    @FXML private TableColumn<OperationLog, String> colTargetType;
    @FXML private TableColumn<OperationLog, String> colDetail;
    @FXML private TableColumn<OperationLog, String> colIp;
    @FXML private TableColumn<OperationLog, String> colStatus;
    @FXML private TableColumn<OperationLog, Long> colDuration;
    @FXML private TableColumn<OperationLog, String> colCreatedAt;
    @FXML private Pagination pagination;

    private final ObservableList<OperationLog> tableData = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colOperation.setCellValueFactory(new PropertyValueFactory<>("operation"));
        colTargetType.setCellValueFactory(new PropertyValueFactory<>("targetType"));
        colDetail.setCellValueFactory(new PropertyValueFactory<>("detail"));
        colIp.setCellValueFactory(new PropertyValueFactory<>("ip"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("resultStatus"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("durationMs"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        table.setItems(tableData);
        loadData();
    }

    @FXML
    private void handleSearch() { currentPage = 1; loadData(); }

    private void loadData() {
        Task<PageResult<OperationLog>> task = new Task<>() {
            @Override
            protected PageResult<OperationLog> call() throws Exception {
                return OperationLogService.getPage(currentPage, pageSize,
                        usernameField.getText(), operationField.getText(),
                        startDateField.getText(), endDateField.getText());
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<OperationLog> result = task.getValue();
            tableData.clear(); tableData.addAll(result.getRecords());
            totalItems = result.getTotal(); updatePagination();
        });
        task.setOnFailed(e -> CrudHelper.showError("加载失败"));
        new Thread(task).start();
    }

    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) totalItems / pageSize);
        if (pageCount < 1) pageCount = 1;
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(currentPage - 1);
        pagination.setPageFactory(pageIndex -> {
            if (pageIndex + 1 != currentPage) { currentPage = pageIndex + 1; loadData(); }
            return new Label("");
        });
    }
}
