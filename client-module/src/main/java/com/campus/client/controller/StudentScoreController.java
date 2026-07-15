package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.Score;
import com.campus.client.service.ScoreService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class StudentScoreController implements Initializable {

    @FXML private TableView<Score> myScoreTable;
    @FXML private TableColumn<Score, String> colCourseName;
    @FXML private TableColumn<Score, String> colExamName;
    @FXML private TableColumn<Score, Double> colScore;
    @FXML private TableColumn<Score, Double> colRank;
    @FXML private TableColumn<Score, String> colCreateTime;

    private final ScoreService scoreService = new ScoreService();
    private final ObservableList<Score> scoreData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colExamName.setCellValueFactory(new PropertyValueFactory<>("examName"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colCreateTime.setCellValueFactory(new PropertyValueFactory<>("createTime"));
        myScoreTable.setItems(scoreData);
    }

    private void loadData() {
        Task<ApiResult<PageResult<Score>>> task = new Task<>() {
            @Override protected ApiResult<PageResult<Score>> call() throws Exception {
                return scoreService.getScores(0, 100, null, null);
            }
        };
        task.setOnSucceeded(e -> {
            ApiResult<PageResult<Score>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) {
                scoreData.setAll(result.getData().getContent());
            }
        });
        new Thread(task).start();
    }
}
