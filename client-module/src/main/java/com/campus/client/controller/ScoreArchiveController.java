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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class ScoreArchiveController implements Initializable {

    @FXML private TableView<Score> archiveTable;
    @FXML private TableColumn<Score, Long> colId;
    @FXML private TableColumn<Score, String> colStudentName;
    @FXML private TableColumn<Score, String> colCourseName;
    @FXML private TableColumn<Score, String> colExamName;
    @FXML private TableColumn<Score, Double> colScore;
    @FXML private TableColumn<Score, String> colStatus;
    @FXML private TableColumn<Score, Void> colActions;
    @FXML private Label totalLabel;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;

    private final ScoreService scoreService = new ScoreService();
    private int currentPage = 0;
    private int totalPages = 1;
    private final ObservableList<Score> scoreData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colExamName.setCellValueFactory(new PropertyValueFactory<>("examName"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus() != null && cellData.getValue().getStatus() == 1 ? "已归档" : "未归档"));
        colActions.setCellFactory(col -> new TableCell<>() {
            {
                Button archiveBtn = new Button("归档");
                archiveBtn.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;");
                archiveBtn.setOnAction(e -> handleArchive(getTableView().getItems().get(getIndex())));
                setGraphic(archiveBtn);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });
        archiveTable.setItems(scoreData);
    }

    private void handleArchive(Score score) {
        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception { scoreService.updateScoreStatus(score.getId(), 1); return null; }
        };
        t.setOnSucceeded(e -> loadData());
        new Thread(t).start();
    }

    private void loadData() {
        Task<ApiResult<PageResult<Score>>> task = new Task<>() {
            @Override protected ApiResult<PageResult<Score>> call() throws Exception { return scoreService.getArchiveOverview(currentPage, 15); }
        };
        task.setOnSucceeded(e -> {
            ApiResult<PageResult<Score>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) {
                scoreData.setAll(result.getData().getContent());
                totalPages = Math.max(1, result.getData().getTotalPages());
                pageInfo.setText("第 " + (currentPage + 1) + " 页 / 共 " + totalPages + " 页");
                totalLabel.setText("共 " + result.getData().getTotalElements() + " 条");
            }
        });
        new Thread(task).start();
    }

    @FXML private void handlePrevPage() { if (currentPage > 0) { currentPage--; loadData(); } }
    @FXML private void handleNextPage() { if (currentPage < totalPages - 1) { currentPage++; loadData(); } }
}
