package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.Diagnosis;
import com.campus.client.service.DiagnosisService;
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

public class TeacherDiagnosisController implements Initializable {

    @FXML private TableView<Diagnosis> diagnosisTable;
    @FXML private TableColumn<Diagnosis, Long> colId;
    @FXML private TableColumn<Diagnosis, String> colStudentName;
    @FXML private TableColumn<Diagnosis, String> colCourseName;
    @FXML private TableColumn<Diagnosis, String> colDiagnosisType;
    @FXML private TableColumn<Diagnosis, String> colContent;
    @FXML private TableColumn<Diagnosis, String> colSuggestion;
    @FXML private TableColumn<Diagnosis, String> colCreateTime;

    private final DiagnosisService diagnosisService = new DiagnosisService();
    private final ObservableList<Diagnosis> diagnosisData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colDiagnosisType.setCellValueFactory(new PropertyValueFactory<>("diagnosisType"));
        colContent.setCellValueFactory(new PropertyValueFactory<>("content"));
        colSuggestion.setCellValueFactory(new PropertyValueFactory<>("suggestion"));
        colCreateTime.setCellValueFactory(new PropertyValueFactory<>("createTime"));
        diagnosisTable.setItems(diagnosisData);
    }

    @FXML
    private void handleAdd() {
        AlertHelper.showInfo("新建诊断", "请填写学业诊断信息后提交");
    }

    private void loadData() {
        Task<ApiResult<List<Diagnosis>>> task = new Task<>() {
            @Override protected ApiResult<List<Diagnosis>> call() throws Exception { return diagnosisService.getMyDiagnosis(); }
        };
        task.setOnSucceeded(e -> {
            ApiResult<List<Diagnosis>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) diagnosisData.setAll(result.getData());
        });
        new Thread(task).start();
    }
}
