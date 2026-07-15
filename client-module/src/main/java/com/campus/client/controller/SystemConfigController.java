package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.Dict;
import com.campus.client.model.SystemConfig;
import com.campus.client.service.DictService;
import com.campus.client.service.SystemConfigService;
import com.campus.client.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SystemConfigController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private TableView<SystemConfig> configTable;
    @FXML private TableColumn<SystemConfig, String> colConfigKey;
    @FXML private TableColumn<SystemConfig, String> colConfigValue;
    @FXML private TableColumn<SystemConfig, String> colDescription;
    @FXML private TableView<Dict> dictTable;
    @FXML private TableColumn<Dict, String> colDictType;
    @FXML private TableColumn<Dict, String> colDictCode;
    @FXML private TableColumn<Dict, String> colDictLabel;
    @FXML private TableColumn<Dict, Integer> colSortOrder;
    @FXML private TableColumn<Dict, String> colDictStatus;

    private final SystemConfigService configService = new SystemConfigService();
    private final DictService dictService = new DictService();
    private final ObservableList<SystemConfig> configData = FXCollections.observableArrayList();
    private final ObservableList<Dict> dictData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupConfigTable();
        setupDictTable();
        loadConfigs();
        loadDicts();
    }

    private void setupConfigTable() {
        colConfigKey.setCellValueFactory(new PropertyValueFactory<>("configKey"));
        colConfigValue.setCellValueFactory(new PropertyValueFactory<>("configValue"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        configTable.setEditable(true);
        configTable.setItems(configData);
    }

    private void setupDictTable() {
        colDictType.setCellValueFactory(new PropertyValueFactory<>("dictType"));
        colDictCode.setCellValueFactory(new PropertyValueFactory<>("dictCode"));
        colDictLabel.setCellValueFactory(new PropertyValueFactory<>("dictLabel"));
        colSortOrder.setCellValueFactory(new PropertyValueFactory<>("sortOrder"));
        colDictStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus() != null && cellData.getValue().getStatus() == 1 ? "启用" : "禁用"));
        dictTable.setItems(dictData);
    }

    @FXML
    private void handleSaveConfigs() {
        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception {
                configService.updateConfigs(configData);
                return null;
            }
        };
        t.setOnSucceeded(e -> AlertHelper.showInfo("成功", "配置已保存"));
        new Thread(t).start();
    }

    @FXML
    private void handleAddDict() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("新增字典");
        dialog.setHeaderText("输入字典标签");
        dialog.setContentText("标签:");
        dialog.showAndWait().ifPresent(label -> {
            Dict dict = new Dict();
            dict.setDictLabel(label);
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception { dictService.createDict(dict); return null; }
            };
            t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "字典已添加"); loadDicts(); });
            new Thread(t).start();
        });
    }

    private void loadConfigs() {
        Task<ApiResult<List<SystemConfig>>> task = new Task<>() {
            @Override protected ApiResult<List<SystemConfig>> call() throws Exception { return configService.getConfigs(); }
        };
        task.setOnSucceeded(e -> {
            ApiResult<List<SystemConfig>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) configData.setAll(result.getData());
        });
        new Thread(task).start();
    }

    private void loadDicts() {
        Task<ApiResult<List<Dict>>> task = new Task<>() {
            @Override protected ApiResult<List<Dict>> call() throws Exception { return dictService.getDicts(); }
        };
        task.setOnSucceeded(e -> {
            ApiResult<List<Dict>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) dictData.setAll(result.getData());
        });
        new Thread(task).start();
    }
}
