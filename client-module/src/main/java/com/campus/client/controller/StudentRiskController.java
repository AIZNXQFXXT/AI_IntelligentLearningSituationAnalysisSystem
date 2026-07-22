package com.campus.client.controller;

import com.campus.client.model.RiskWarning;
import com.campus.client.service.RiskWarningService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import com.campus.client.util.TableUtils;
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

        TableUtils.setupRiskLevelCell(colRiskLevel);
        TableUtils.setupStatusCell(colHandleStatus);

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
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }
}
