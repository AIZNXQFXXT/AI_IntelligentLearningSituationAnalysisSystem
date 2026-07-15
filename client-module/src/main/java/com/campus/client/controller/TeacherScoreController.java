package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.Score;
import com.campus.client.service.ScoreService;
import com.campus.client.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class TeacherScoreController implements Initializable {

    @FXML private ComboBox<String> examFilter;
    @FXML private ComboBox<String> courseFilter;
    @FXML private TableView<Score> scoreTable;
    @FXML private TableColumn<Score, Long> colId;
    @FXML private TableColumn<Score, String> colStudentNo;
    @FXML private TableColumn<Score, String> colStudentName;
    @FXML private TableColumn<Score, String> colCourseName;
    @FXML private TableColumn<Score, String> colExamName;
    @FXML private TableColumn<Score, Double> colScore;
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
        colStudentNo.setCellValueFactory(new PropertyValueFactory<>("studentNo"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colExamName.setCellValueFactory(new PropertyValueFactory<>("examName"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colActions.setCellFactory(col -> new TableCell<>() {
            {
                Button editBtn = new Button("编辑");
                editBtn.setStyle("-fx-background-color: #1E88E5; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                setGraphic(editBtn);
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });
        scoreTable.setItems(scoreData);
    }

    @FXML
    private void handleSearch() { currentPage = 0; loadData(); }

    @FXML
    private void handleAdd() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("录入成绩");
        dialog.setHeaderText("输入成绩分数");
        dialog.setContentText("成绩:");
        dialog.showAndWait().ifPresent(s -> {
            try {
                double scoreVal = Double.parseDouble(s);
                Score sc = new Score();
                sc.setScore(scoreVal);
                Task<Void> t = new Task<>() {
                    @Override protected Void call() throws Exception { scoreService.submitScore(sc); return null; }
                };
                t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "成绩已录入"); loadData(); });
                new Thread(t).start();
            } catch (NumberFormatException ex) {
                AlertHelper.showError("错误", "请输入有效数字");
            }
        });
    }

    @FXML
    private void handleBatchAdd() {
        AlertHelper.showInfo("批量录入", "请通过后端接口 /api/scores/batch 批量录入");
    }

    private void handleEdit(Score score) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(score.getScore()));
        dialog.setTitle("编辑成绩");
        dialog.setContentText("成绩:");
        dialog.showAndWait().ifPresent(s -> {
            try {
                score.setScore(Double.parseDouble(s));
                Task<Void> t = new Task<>() {
                    @Override protected Void call() throws Exception { scoreService.updateScore(score.getId(), score); return null; }
                };
                t.setOnSucceeded(e -> { AlertHelper.showInfo("成功", "已更新"); loadData(); });
                new Thread(t).start();
            } catch (NumberFormatException ex) {
                AlertHelper.showError("错误", "请输入有效数字");
            }
        });
    }

    private void loadData() {
        Task<ApiResult<PageResult<Score>>> task = new Task<>() {
            @Override protected ApiResult<PageResult<Score>> call() throws Exception {
                return scoreService.getScores(currentPage, 15, null, null);
            }
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
