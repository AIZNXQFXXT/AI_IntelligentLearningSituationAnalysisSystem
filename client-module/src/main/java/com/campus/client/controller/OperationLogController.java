package com.campus.client.controller;

import com.campus.client.model.OperationLog;
import com.campus.client.model.PageResult;
import com.campus.client.service.OperationLogService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class OperationLogController {

    @FXML private TextField usernameField;
    @FXML private TextField operationField;
    @FXML private DatePicker startDateField;
    @FXML private DatePicker endDateField;
    @FXML private TableView<OperationLog> table;
    @FXML private TableColumn<OperationLog, String> colUsername;
    @FXML private TableColumn<OperationLog, String> colOperation;
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
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colOperation.setCellValueFactory(new PropertyValueFactory<>("operation"));
        colDetail.setCellValueFactory(new PropertyValueFactory<>("detail"));
        colIp.setCellValueFactory(new PropertyValueFactory<>("ip"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("resultStatus"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("durationMs"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        table.setItems(tableData);
        // 分页工厂只注册一次：用户点页码时由它触发加载
        pagination.setPageFactory(this::buildPage);
        loadData();
    }

    @FXML
    private void handleSearch() {
        currentPage = 1;
        // 视觉回到第 1 页（若已在第 1 页， setCurrentPageIndex 为 no-op，下方 loadData 兜底）
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
        Task<PageResult<OperationLog>> task = new Task<>() {
            @Override
            protected PageResult<OperationLog> call() throws Exception {
                LocalDate start = startDateField.getValue();
                LocalDate end = endDateField.getValue();
                return OperationLogService.getPage(currentPage, pageSize,
                        usernameField.getText(), operationField.getText(),
                        start == null ? "" : start.toString(),
                        end == null ? "" : end.toString());
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<OperationLog> result = task.getValue();
            tableData.clear();
            tableData.addAll(result.getRecords());
            totalItems = result.getTotal();
            int pageCount = (int) Math.ceil((double) totalItems / pageSize);
            if (pageCount < 1) pageCount = 1;
            pagination.setPageCount(pageCount);
            // 关键：此处不再调用 setCurrentPageIndex / setPageFactory。
            // 否则每个异步响应回来都会把分页指针拽回"本次响应的页"，触发连环重载。
            // 指针由用户点击驱动，搜索重置由 handleSearch 处理。
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }
}
