package com.aicampus.controller;

import com.aicampus.model.SysConfig;
import com.aicampus.service.ConfigService;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.List;

public class ConfigController {

    @FXML private TableView<SysConfig> table;
    @FXML private TableColumn<SysConfig, Integer> colId;
    @FXML private TableColumn<SysConfig, String> colKey;
    @FXML private TableColumn<SysConfig, String> colValue;
    @FXML private TableColumn<SysConfig, String> colDesc;
    @FXML private TableColumn<SysConfig, Void> colAction;

    private final ObservableList<SysConfig> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colKey.setCellValueFactory(new PropertyValueFactory<>("configKey"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("configValue"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        colValue.setCellFactory(param -> new TableCell<>() {
            private final TextField textField = new TextField();
            {
                textField.setStyle("-fx-font-size: 13px;");
                textField.textProperty().addListener((obs, old, val) -> {
                    getTableView().getItems().get(getIndex()).setConfigValue(val);
                });
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); } else { textField.setText(item); setGraphic(textField); }
            }
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button saveBtn = new Button("保存");
            {
                saveBtn.getStyleClass().addAll("btn-primary", "btn-sm");
                saveBtn.setOnAction(e -> handleSave(getTableView().getItems().get(getIndex())));
                HBox box = new HBox(saveBtn); box.setAlignment(Pos.CENTER); setGraphic(box);
            }
            @Override
            protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : getGraphic()); }
        });

        table.setItems(tableData);
        loadData();
    }

    private void loadData() {
        Task<List<SysConfig>> task = new Task<>() {
            @Override
            protected List<SysConfig> call() throws Exception { return ConfigService.getAll(); }
        };
        task.setOnSucceeded(e -> { tableData.clear(); tableData.addAll(task.getValue()); });
        task.setOnFailed(e -> CrudHelper.showError("加载失败"));
        new Thread(task).start();
    }

    private void handleSave(SysConfig item) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception { ConfigService.update(item.getId(), item); return null; }
        };
        task.setOnSucceeded(e -> CrudHelper.showAlert("保存成功"));
        task.setOnFailed(e -> CrudHelper.showError("保存失败"));
        new Thread(task).start();
    }
}
