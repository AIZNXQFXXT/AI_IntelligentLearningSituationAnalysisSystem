package com.aicampus.controller;

import com.aicampus.model.RiskWarning;
import com.aicampus.service.RiskWarningService;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.Arrays;
import java.util.List;

public class TeacherRiskController {

    @FXML private TableView<RiskWarning> table;
    @FXML private TableColumn<RiskWarning, Integer> colId;
    @FXML private TableColumn<RiskWarning, Integer> colStudentId;
    @FXML private TableColumn<RiskWarning, String> colSemester;
    @FXML private TableColumn<RiskWarning, String> colRiskLevel;
    @FXML private TableColumn<RiskWarning, String> colReason;
    @FXML private TableColumn<RiskWarning, String> colStatus;
    @FXML private TableColumn<RiskWarning, Void> colAction;
    @FXML private ComboBox<String> semesterCombo;
    @FXML private ComboBox<String> riskLevelCombo;
    @FXML private ComboBox<String> statusCombo;

    private final ObservableList<RiskWarning> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        semesterCombo.setItems(FXCollections.observableArrayList(Arrays.asList("2025-1", "2024-2", "2024-1")));
        semesterCombo.getSelectionModel().selectFirst();
        riskLevelCombo.setItems(FXCollections.observableArrayList(Arrays.asList("全部", "HIGH", "MEDIUM", "LOW")));
        riskLevelCombo.getSelectionModel().selectFirst();
        statusCombo.setItems(FXCollections.observableArrayList(Arrays.asList("全部", "UNHANDLED", "HANDLED")));
        statusCombo.getSelectionModel().selectFirst();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colRiskLevel.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("riskReason"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("handleStatus"));

        colRiskLevel.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label label = new Label(item);
                switch (item) {
                    case "HIGH" -> label.getStyleClass().add("tag-danger");
                    case "MEDIUM" -> label.getStyleClass().add("tag-warning");
                    default -> label.getStyleClass().add("tag-success");
                }
                setGraphic(label);
            }
        });

        colStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label label = new Label("HANDLED".equals(item) ? "已处理" : "未处理");
                label.getStyleClass().add("HANDLED".equals(item) ? "tag-success" : "tag-danger");
                setGraphic(label);
            }
        });

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
        String riskLevel = "全部".equals(riskLevelCombo.getValue()) ? null : riskLevelCombo.getValue();
        String status = "全部".equals(statusCombo.getValue()) ? null : statusCombo.getValue();

        Task<List<RiskWarning>> task = new Task<>() {
            @Override protected List<RiskWarning> call() throws Exception {
                return RiskWarningService.getPage(1, 50, semester, riskLevel, status).getRecords();
            }
        };
        task.setOnSucceeded(e -> { tableData.clear(); tableData.addAll(task.getValue()); });
        task.setOnFailed(e -> CrudHelper.showError("加载失败"));
        new Thread(task).start();
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
            new Thread(task).start();
        });
    }
}
