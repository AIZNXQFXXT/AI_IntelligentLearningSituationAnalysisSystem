package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.RiskWarning;
import com.campus.client.service.RiskWarningService;
import com.campus.client.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class TeacherRiskController implements Initializable {

    @FXML private ComboBox<String> handledFilter;
    @FXML private TableView<RiskWarning> warningTable;
    @FXML private TableColumn<RiskWarning, Long> colId;
    @FXML private TableColumn<RiskWarning, String> colStudentName;
    @FXML private TableColumn<RiskWarning, String> colClassName;
    @FXML private TableColumn<RiskWarning, String> colWarningType;
    @FXML private TableColumn<RiskWarning, String> colLevel;
    @FXML private TableColumn<RiskWarning, String> colContent;
    @FXML private TableColumn<RiskWarning, String> colHandled;
    @FXML private TableColumn<RiskWarning, String> colCreateTime;
    @FXML private TableColumn<RiskWarning, Void> colActions;
    @FXML private Label pageInfo;

    private final RiskWarningService warningService = new RiskWarningService();
    private int currentPage = 0;
    private int totalPages = 1;
    private final ObservableList<RiskWarning> warningData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        handledFilter.setItems(FXCollections.observableArrayList("全部", "未处理", "已处理"));
        handledFilter.getSelectionModel().selectFirst();
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("className"));
        colWarningType.setCellValueFactory(new PropertyValueFactory<>("warningType"));
        colLevel.setCellValueFactory(new PropertyValueFactory<>("level"));
        colContent.setCellValueFactory(new PropertyValueFactory<>("content"));
        colHandled.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getHandled() != null && cellData.getValue().getHandled() == 1 ? "已处理" : "未处理"));
        colCreateTime.setCellValueFactory(new PropertyValueFactory<>("createTime"));
        colActions.setCellFactory(col -> new TableCell<>() {
            {
                Button handleBtn = new Button("处理");
                handleBtn.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;");
                handleBtn.setOnAction(e -> handleProcess(getTableView().getItems().get(getIndex())));
                setGraphic(handleBtn);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });
        warningTable.setItems(warningData);
    }

    @FXML
    private void handleSearch() { currentPage = 0; loadData(); }

    private void handleProcess(RiskWarning warning) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("处理预警");
        dialog.setHeaderText("输入处理结果");
        dialog.setContentText("处理结果:");
        dialog.showAndWait().ifPresent(result -> {
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception { warningService.handleWarning(warning.getId(), result); return null; }
            };
            t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "预警已处理"); loadData(); });
            new Thread(t).start();
        });
    }

    private void loadData() {
        String filter = handledFilter.getValue();
        Integer handled;
        if ("未处理".equals(filter)) handled = 0;
        else if ("已处理".equals(filter)) handled = 1;
        else {
            handled = null;
        }

        Task<ApiResult<PageResult<RiskWarning>>> task = new Task<>() {
            @Override protected ApiResult<PageResult<RiskWarning>> call() throws Exception {
                return warningService.getRiskWarnings(currentPage, 15, handled);
            }
        };
        task.setOnSucceeded(e -> {
            ApiResult<PageResult<RiskWarning>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) {
                warningData.setAll(result.getData().getContent());
                totalPages = Math.max(1, result.getData().getTotalPages());
                pageInfo.setText("第 " + (currentPage + 1) + " 页 / 共 " + totalPages + " 页");
            }
        });
        new Thread(task).start();
    }

    @FXML private void handlePrevPage() { if (currentPage > 0) { currentPage--; loadData(); } }
    @FXML private void handleNextPage() { if (currentPage < totalPages - 1) { currentPage++; loadData(); } }
}
