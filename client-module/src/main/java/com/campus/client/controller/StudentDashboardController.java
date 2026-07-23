package com.campus.client.controller;

import com.campus.client.model.PageResult;
import com.campus.client.model.RiskWarning;
import com.campus.client.model.Score;
import com.campus.client.service.RiskWarningService;
import com.campus.client.service.ScoreService;
import com.campus.client.session.UserSession;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class StudentDashboardController {

    @FXML private Label valueAvgScore;
    @FXML private Label valueWarnings;
    @FXML private Label valueUsername;
    @FXML private TableView<Score> scoreTable;
    @FXML private TableColumn<Score, String> colCourseName;
    @FXML private TableColumn<Score, Double> colFinalScore;
    @FXML private TableColumn<Score, Integer> colRankClass;
    @FXML private TableColumn<Score, Integer> colRankGrade;
    @FXML private TableView<RiskWarning> warningTable;
    @FXML private TableColumn<RiskWarning, String> colSemester;
    @FXML private TableColumn<RiskWarning, String> colRiskLevel;
    @FXML private TableColumn<RiskWarning, String> colRiskReason;
    @FXML private TableColumn<RiskWarning, String> colHandleStatus;
    @FXML private Label noScoreLabel;
    @FXML private Label noWarningLabel;

    private final ObservableList<Score> scoreData = FXCollections.observableArrayList();
    private final ObservableList<RiskWarning> warningData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        valueUsername.setText(UserSession.getInstance().getUsername());

        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colFinalScore.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
        colRankClass.setCellValueFactory(new PropertyValueFactory<>("rankClass"));
        colRankGrade.setCellValueFactory(new PropertyValueFactory<>("rankGrade"));
        scoreTable.setItems(scoreData);

        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colRiskLevel.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        colRiskReason.setCellValueFactory(new PropertyValueFactory<>("riskReason"));
        colHandleStatus.setCellValueFactory(new PropertyValueFactory<>("handleStatus"));
        warningTable.setItems(warningData);

        loadData();
    }

    private void loadData() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                PageResult<Score> scoreRes = ScoreService.getMyScores(1, 5);
                List<RiskWarning> warnings = RiskWarningService.getMyWarnings();

                Platform.runLater(() -> {
                    List<Score> scores = scoreRes.getRecords().size() > 5
                            ? scoreRes.getRecords().subList(0, 5)
                            : scoreRes.getRecords();
                    scoreData.addAll(scores);
                    if (scores.isEmpty()) {
                        noScoreLabel.setVisible(true);
                        noScoreLabel.setManaged(true);
                        scoreTable.setVisible(false);
                        scoreTable.setManaged(false);
                    } else {
                        double avg = scores.stream()
                                .mapToDouble(Score::getFinalScore)
                                .average().orElse(0);
                        valueAvgScore.setText(String.format("%.1f", avg));
                    }

                    List<RiskWarning> recentWarnings = warnings.size() > 5
                            ? warnings.subList(0, 5)
                            : warnings;
                    warningData.addAll(recentWarnings);
                    // 预警数量只统计中风险(MEDIUM)和高风险(HIGH/CRITICAL)
                    long effectiveWarnings = warnings.stream()
                            .filter(w -> {
                                String lvl = w.getRiskLevel();
                                return "HIGH".equals(lvl) || "MEDIUM".equals(lvl) || "CRITICAL".equals(lvl);
                            })
                            .count();
                    valueWarnings.setText(String.valueOf(effectiveWarnings));
                    if (recentWarnings.isEmpty()) {
                        noWarningLabel.setVisible(true);
                        noWarningLabel.setManaged(true);
                        warningTable.setVisible(false);
                        warningTable.setManaged(false);
                    }
                });
                return null;
            }
        };

        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载仪表盘数据失败");
        });

        AppExecutors.submit(task::run);
    }

    @FXML private void goToScores(javafx.event.ActionEvent e) { navigateTo("/student/scores"); }
    @FXML private void goToAnalysis(javafx.event.ActionEvent e) { navigateTo("/student/analysis"); }
    @FXML private void goToDiagnosis(javafx.event.ActionEvent e) { navigateTo("/student/diagnosis"); }
    @FXML private void goToComment(javafx.event.ActionEvent e) { navigateTo("/student/comment"); }
    @FXML private void goToRisk(javafx.event.ActionEvent e) { navigateTo("/student/risk"); }

    private void navigateTo(String route) {
        MainLayoutController mainLayout = MainLayoutController.getInstance();
        if (mainLayout != null) {
            mainLayout.navigateTo(route);
        }
    }
}
