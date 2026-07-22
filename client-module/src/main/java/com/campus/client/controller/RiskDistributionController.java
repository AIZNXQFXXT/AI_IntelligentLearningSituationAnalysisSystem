package com.aicampus.controller;

import com.aicampus.model.ApiResponse;
import com.aicampus.model.ClassInfo;
import com.aicampus.service.ApiClient;
import com.aicampus.service.ClassService;
import com.aicampus.util.AppExecutors;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;

import java.util.List;
import java.util.Map;

public class RiskDistributionController {

    @FXML private ComboBox<String> gradeFilter;
    @FXML private Label valueHighRisk;
    @FXML private Label valueMediumRisk;
    @FXML private Label valueLowRisk;
    @FXML private Label valueTotalRisk;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private TableView<Map<String, Object>> dataTable;
    @FXML private TableColumn<Map<String, Object>, String> colName;
    @FXML private TableColumn<Map<String, Object>, Number> colHighCount;
    @FXML private TableColumn<Map<String, Object>, Number> colMediumCount;
    @FXML private TableColumn<Map<String, Object>, Number> colLowCount;
    @FXML private TableColumn<Map<String, Object>, Number> colTotal;
    @FXML private Label noDataLabel;

    private final ObservableList<Map<String, Object>> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(data.getValue().getOrDefault("dimensionValue", ""))));
        colHighCount.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
                Integer.parseInt(String.valueOf(data.getValue().getOrDefault("highRiskCount", 0)))));
        colMediumCount.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
                Integer.parseInt(String.valueOf(data.getValue().getOrDefault("mediumRiskCount", 0)))));
        colLowCount.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
                Integer.parseInt(String.valueOf(data.getValue().getOrDefault("lowRiskCount", 0)))));
        colTotal.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(
                Integer.parseInt(String.valueOf(data.getValue().getOrDefault("totalCount", 0)))));
        dataTable.setItems(tableData);

        loadGrades();
    }

    private void loadGrades() {
        Task<List<ClassInfo>> task = new Task<>() {
            @Override protected List<ClassInfo> call() throws Exception {
                return ClassService.getAll();
            }
        };
        task.setOnSucceeded(e -> {
            List<String> grades = task.getValue().stream()
                    .map(ClassInfo::getGrade)
                    .filter(g -> g != null && !g.isEmpty())
                    .distinct()
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
            gradeFilter.getItems().addAll(grades);
        });
        AppExecutors.submit(task::run);
    }

    @FXML
    private void handleQuery() {
        String grade = gradeFilter.getSelectionModel().getSelectedItem();
        String gradeParam = grade != null ? "&grade=" + ApiClient.encodeParam(grade) : "";

        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                String path = "/academic-stats/risk-distribution?groupBy=grade" + gradeParam;
                return ApiClient.get(path, new TypeReference<ApiResponse<List<Map<String, Object>>>>() {});
            }
        };
        task.setOnSucceeded(e -> {
            List<Map<String, Object>> result = task.getValue();
            tableData.clear();
            pieChart.getData().clear();
            barChart.getData().clear();

            if (result != null && !result.isEmpty()) {
                tableData.addAll(result);
                noDataLabel.setVisible(false);
                noDataLabel.setManaged(false);
                dataTable.setVisible(true);
                dataTable.setManaged(true);

                int totalHigh = 0, totalMedium = 0, totalLow = 0;

                // Pie chart for overall distribution
                XYChart.Series<String, Number> barSeries = new XYChart.Series<>();

                for (Map<String, Object> row : result) {
                    int high = Integer.parseInt(String.valueOf(row.getOrDefault("highRiskCount", 0)));
                    int medium = Integer.parseInt(String.valueOf(row.getOrDefault("mediumRiskCount", 0)));
                    int low = Integer.parseInt(String.valueOf(row.getOrDefault("lowRiskCount", 0)));
                    totalHigh += high;
                    totalMedium += medium;
                    totalLow += low;

                    String dv = String.valueOf(row.getOrDefault("dimensionValue", ""));
                    barSeries.getData().add(new XYChart.Data<>(dv, high + medium + low));
                }

                pieChart.getData().add(new PieChart.Data("高风险", totalHigh));
                pieChart.getData().add(new PieChart.Data("中风险", totalMedium));
                pieChart.getData().add(new PieChart.Data("低风险", totalLow));

                barChart.getData().add(barSeries);

                valueHighRisk.setText(String.valueOf(totalHigh));
                valueMediumRisk.setText(String.valueOf(totalMedium));
                valueLowRisk.setText(String.valueOf(totalLow));
                valueTotalRisk.setText(String.valueOf(totalHigh + totalMedium + totalLow));
            } else {
                noDataLabel.setVisible(true);
                noDataLabel.setManaged(true);
                dataTable.setVisible(false);
                dataTable.setManaged(false);
                valueHighRisk.setText("--");
                valueMediumRisk.setText("--");
                valueLowRisk.setText("--");
                valueTotalRisk.setText("--");
            }
        });
        task.setOnFailed(e -> {
            tableData.clear();
            pieChart.getData().clear();
            barChart.getData().clear();
            noDataLabel.setVisible(true);
            noDataLabel.setManaged(true);
            dataTable.setVisible(false);
            dataTable.setManaged(false);
        });
        AppExecutors.submit(task::run);
    }
}
