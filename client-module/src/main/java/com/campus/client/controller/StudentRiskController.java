package com.campus.client.controller;

import com.campus.client.model.PageResult;
import com.campus.client.model.RiskWarning;
import com.campus.client.service.RiskWarningService;
import com.campus.client.service.SemesterService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import com.campus.client.util.TableUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class StudentRiskController {

    @FXML private TableView<RiskWarning> table;
    @FXML private TableColumn<RiskWarning, String> colSemester;
    @FXML private TableColumn<RiskWarning, String> colRiskLevel;
    @FXML private TableColumn<RiskWarning, String> colReason;
    @FXML private TableColumn<RiskWarning, String> colHandleStatus;
    @FXML private TableColumn<RiskWarning, String> colRemark;
    @FXML private ComboBox<String> semesterFilter;
    @FXML private ComboBox<String> riskLevelFilter;
    @FXML private Pagination pagination;
    @FXML private Label emptyLabel;

    private final ObservableList<RiskWarning> tableData = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

    @FXML
    public void initialize() {
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colRiskLevel.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("riskReason"));
        colHandleStatus.setCellValueFactory(new PropertyValueFactory<>("handleStatus"));
        colRemark.setCellValueFactory(new PropertyValueFactory<>("handleRemark"));

        TableUtils.setupRiskLevelCell(colRiskLevel);
        TableUtils.setupStatusCell(colHandleStatus);

        table.setItems(tableData);

        semesterFilter.setOnAction(e -> handleFilterChange());
        riskLevelFilter.setOnAction(e -> handleFilterChange());
        loadSemesters();
        setupRiskLevelFilter();

        pagination.setPageFactory(this::buildPage);
    }

    private void loadSemesters() {
        Task<List<String>> task = new Task<>() {
            @Override protected List<String> call() throws Exception {
                return SemesterService.getAllSemesters();
            }
        };
        task.setOnSucceeded(e -> {
            List<String> semesters = new java.util.ArrayList<>(task.getValue());
            semesters.add(0, "全部");
            semesterFilter.setItems(FXCollections.observableArrayList(semesters));
            EventHandler<ActionEvent> semesterHandler = semesterFilter.getOnAction();
            EventHandler<ActionEvent> riskLevelHandler = riskLevelFilter.getOnAction();
            semesterFilter.setOnAction(null);
            riskLevelFilter.setOnAction(null);
            semesterFilter.getSelectionModel().selectFirst();
            semesterFilter.setOnAction(semesterHandler);
            riskLevelFilter.setOnAction(riskLevelHandler);
            loadData();
        });
        task.setOnFailed(e -> {
            semesterFilter.setItems(FXCollections.observableArrayList("全部", "2025-1", "2024-2", "2024-1"));
            EventHandler<ActionEvent> semesterHandler = semesterFilter.getOnAction();
            EventHandler<ActionEvent> riskLevelHandler = riskLevelFilter.getOnAction();
            semesterFilter.setOnAction(null);
            riskLevelFilter.setOnAction(null);
            semesterFilter.getSelectionModel().selectFirst();
            semesterFilter.setOnAction(semesterHandler);
            riskLevelFilter.setOnAction(riskLevelHandler);
            loadData();
        });
        AppExecutors.submit(task::run);
    }

    private void setupRiskLevelFilter() {
        riskLevelFilter.setItems(FXCollections.observableArrayList("全部", "高风险", "中风险", "低风险"));
        EventHandler<ActionEvent> handler = riskLevelFilter.getOnAction();
        riskLevelFilter.setOnAction(null);
        riskLevelFilter.getSelectionModel().selectFirst();
        riskLevelFilter.setOnAction(handler);
    }

    private void handleFilterChange() {
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
        String semester = semesterFilter.getValue();
        String semesterParam = "全部".equals(semester) ? null : semester;
        String riskLevel = mapRiskLevel(riskLevelFilter.getValue());
        Task<PageResult<RiskWarning>> task = new Task<>() {
            @Override
            protected PageResult<RiskWarning> call() throws Exception {
                return RiskWarningService.getMyWarnings(currentPage, pageSize, semesterParam, riskLevel);
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<RiskWarning> result = task.getValue();
            List<RiskWarning> warnings = result.getRecords();
            tableData.clear();
            tableData.addAll(warnings);
            emptyLabel.setVisible(warnings.isEmpty());
            emptyLabel.setManaged(warnings.isEmpty());
            table.setVisible(!warnings.isEmpty());
            table.setManaged(!warnings.isEmpty());
            totalItems = result.getTotal();
            int pageCount = (int) Math.ceil((double) totalItems / pageSize);
            if (pageCount < 1) pageCount = 1;
            pagination.setPageCount(pageCount);
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }

    private String mapRiskLevel(String display) {
        if (display == null) return null;
        return switch (display) {
            case "高风险" -> "HIGH";
            case "中风险" -> "MEDIUM";
            case "低风险" -> "LOW";
            default -> null;
        };
    }
}
