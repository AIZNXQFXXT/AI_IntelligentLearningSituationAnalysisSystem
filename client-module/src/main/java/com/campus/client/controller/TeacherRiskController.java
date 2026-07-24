package com.campus.client.controller;

import com.campus.client.model.RiskWarning;
import com.campus.client.service.RiskWarningService;
import com.campus.client.service.SemesterService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import com.campus.client.util.TableUtils;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.util.Arrays;
import java.util.List;

public class TeacherRiskController {

    @FXML private TableView<RiskWarning> table;
    @FXML private TableColumn<RiskWarning, String> colStudentId;
    @FXML private TableColumn<RiskWarning, String> colSemester;
    @FXML private TableColumn<RiskWarning, String> colRiskLevel;
    @FXML private TableColumn<RiskWarning, String> colReason;
    @FXML private TableColumn<RiskWarning, String> colStatus;
    @FXML private TableColumn<RiskWarning, Void> colAction;
    @FXML private ComboBox<String> semesterCombo;
    @FXML private ComboBox<String> riskLevelCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField searchField;

    private final ObservableList<RiskWarning> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadSemesters();
        riskLevelCombo.setItems(FXCollections.observableArrayList(Arrays.asList("全部", "高风险", "中风险", "低风险")));
        riskLevelCombo.getSelectionModel().selectFirst();
        statusCombo.setItems(FXCollections.observableArrayList(Arrays.asList("全部", "待处理", "处理中", "已处理", "已忽略")));
        statusCombo.getSelectionModel().selectFirst();

        colStudentId.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStudentName() + " (" + data.getValue().getStudentNo() + ")"));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colRiskLevel.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("riskReason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("handleStatus"));

        TableUtils.setupRiskLevelCell(colRiskLevel);
        TableUtils.setupStatusCell(colStatus);

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("处理");
            {
                btn.getStyleClass().addAll("btn-primary", "btn-sm");
                btn.setOnAction(e -> handleProcess(getTableView().getItems().get(getIndex())));
                HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn.getParent());
            }
        });

        table.setItems(tableData);
        loadData();
    }

    @FXML
    private void handleSearch() { loadData(); }

    private void loadData() {
        String semester = semesterCombo.getValue();
        String riskLevel = mapRiskLevel(riskLevelCombo.getValue());
        String status = mapStatus(statusCombo.getValue());
        final String keyword = searchField != null && searchField.getText() != null
                ? searchField.getText().trim() : "";

        Task<List<RiskWarning>> task = new Task<>() {
            @Override protected List<RiskWarning> call() throws Exception {
                return RiskWarningService.getPage(1, 50, semester, riskLevel, status, keyword).getRecords();
            }
        };
        task.setOnSucceeded(e -> { tableData.clear(); tableData.addAll(task.getValue()); });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }

    private void loadSemesters() {
        Task<List<String>> task = new Task<>() {
            @Override protected List<String> call() throws Exception {
                return SemesterService.getAllSemesters();
            }
        };
        task.setOnSucceeded(e -> {
            semesterCombo.setItems(FXCollections.observableArrayList(task.getValue()));
            semesterCombo.getSelectionModel().selectFirst();
        });
        task.setOnFailed(e -> {
            semesterCombo.setItems(FXCollections.observableArrayList("2025-1", "2024-2", "2024-1"));
            semesterCombo.getSelectionModel().selectFirst();
        });
        AppExecutors.submit(task::run);
    }

    private String mapRiskLevel(String display) {
        if (display == null || "全部".equals(display)) return null;
        switch (display) {
            case "高风险": return "HIGH";
            case "中风险": return "MEDIUM";
            case "低风险": return "LOW";
            default: return null;
        }
    }

    private String mapStatus(String display) {
        if (display == null || "全部".equals(display)) return null;
        switch (display) {
            case "待处理": return "PENDING";
            case "处理中": return "PROCESSING";
            case "已处理": return "HANDLED";
            case "已忽略": return "IGNORED";
            default: return null;
        }
    }

    private void handleProcess(RiskWarning w) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("处理预警");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextArea textArea = new TextArea();
        textArea.setPromptText("请输入处理备注");
        textArea.setPrefRowCount(4);
        dialog.getDialogPane().setContent(textArea);

        dialog.setResultConverter(bt -> bt == ButtonType.OK ? textArea.getText() : null);
        dialog.showAndWait().ifPresent(remark -> {
            Task<Void> task = new Task<>() {
                @Override protected Void call() throws Exception {
                    RiskWarningService.handle(w.getId(), remark);
                    return null;
                }
            };
            task.setOnSucceeded(e -> { CrudHelper.showAlert("处理成功"); loadData(); });
            task.setOnFailed(e -> CrudHelper.showError("处理失败"));
            AppExecutors.submit(task::run);
        });
    }
}
