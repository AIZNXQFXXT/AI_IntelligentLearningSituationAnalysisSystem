package com.aicampus.controller;

import com.aicampus.model.RiskWarning;
import com.aicampus.service.RiskWarningService;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
    @FXML private Label emptyLabel;

    private final ObservableList<RiskWarning> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colRiskLevel.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("riskReason"));
        colHandleStatus.setCellValueFactory(new PropertyValueFactory<>("handleStatus"));
        colRemark.setCellValueFactory(new PropertyValueFactory<>("handleRemark"));

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

        colHandleStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label("HANDLED".equals(item) ? "已处理" : "未处理");
                    label.getStyleClass().add("HANDLED".equals(item) ? "tag-success" : "tag-danger");
                    setGraphic(label);
                }
            }
        });

        table.setItems(tableData);
        loadData();
    }

    private void loadData() {
        Task<List<RiskWarning>> task = new Task<>() {
            @Override
            protected List<RiskWarning> call() throws Exception {
                return RiskWarningService.getMyWarnings();
            }
        };
        task.setOnSucceeded(e -> {
            List<RiskWarning> warnings = task.getValue();
            tableData.clear();
            tableData.addAll(warnings);
            emptyLabel.setVisible(warnings.isEmpty());
            emptyLabel.setManaged(warnings.isEmpty());
            table.setVisible(!warnings.isEmpty());
            table.setManaged(!warnings.isEmpty());
        });
        task.setOnFailed(e -> CrudHelper.showError("加载失败"));
        new Thread(task).start();
    }
}
