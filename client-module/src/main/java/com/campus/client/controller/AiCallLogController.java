package com.campus.client.controller;

import com.campus.client.model.AiCallLog;
import com.campus.client.model.PageResult;
import com.campus.client.service.AiCallLogService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class AiCallLogController {

    @FXML private TextField functionField;
    @FXML private ComboBox<String> successFilter;
    @FXML private TableView<AiCallLog> table;
    @FXML private TableColumn<AiCallLog, String> colFunction;
    @FXML private TableColumn<AiCallLog, String> colModel;
    @FXML private TableColumn<AiCallLog, String> colCallerRole;
    @FXML private TableColumn<AiCallLog, Integer> colHttpStatus;
    @FXML private TableColumn<AiCallLog, Integer> colTokensIn;
    @FXML private TableColumn<AiCallLog, Integer> colTokensOut;
    @FXML private TableColumn<AiCallLog, String> colCost;
    @FXML private TableColumn<AiCallLog, Long> colDuration;
    @FXML private TableColumn<AiCallLog, String> colSuccess;
    @FXML private TableColumn<AiCallLog, String> colCreatedAt;
    @FXML private Pagination pagination;

    private final ObservableList<AiCallLog> tableData = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

    @FXML
    public void initialize() {
        successFilter.getItems().addAll("全部", "成功", "失败");
        successFilter.setValue("全部");

        colFunction.setCellValueFactory(new PropertyValueFactory<>("functionName"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("aiModel"));
        colCallerRole.setCellValueFactory(new PropertyValueFactory<>("callerRole"));
        colHttpStatus.setCellValueFactory(new PropertyValueFactory<>("httpStatus"));
        colTokensIn.setCellValueFactory(new PropertyValueFactory<>("tokensInput"));
        colTokensOut.setCellValueFactory(new PropertyValueFactory<>("tokensOutput"));
        colCost.setCellValueFactory(cellData ->
                new SimpleStringProperty("$" + String.format("%.4f", cellData.getValue().getEstimatedCost())));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("durationMs"));
        colSuccess.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isSuccess() ? "成功" : "失败"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        table.setItems(tableData);
        // 分页工厂只注册一次：用户点页码时由它触发加载
        pagination.setPageFactory(this::buildPage);
        loadData();
    }

    @FXML
    private void handleSearch() {
        currentPage = 1;
        pagination.setCurrentPageIndex(0);
        loadData();
    }

    private Label buildPage(int pageIndex) {
        int targetPage = pageIndex + 1;
        if (targetPage != currentPage) {
            currentPage = targetPage;
            loadData();
        }
        return new Label("");
    }

    private void loadData() {
        Boolean success = null;
        String sv = successFilter.getValue();
        if ("成功".equals(sv)) success = true;
        else if ("失败".equals(sv)) success = false;
        final Boolean successParam = success;

        Task<PageResult<AiCallLog>> task = new Task<>() {
            @Override
            protected PageResult<AiCallLog> call() throws Exception {
                return AiCallLogService.getPage(currentPage, pageSize, functionField.getText(), successParam);
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<AiCallLog> result = task.getValue();
            tableData.clear(); tableData.addAll(result.getRecords());
            totalItems = result.getTotal();
            int pageCount = (int) Math.ceil((double) totalItems / pageSize);
            if (pageCount < 1) pageCount = 1;
            pagination.setPageCount(pageCount);
            // 此处不再调用 setCurrentPageIndex / setPageFactory，否则异步响应会把指针拽回本次页，触发连环重载
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }
}
