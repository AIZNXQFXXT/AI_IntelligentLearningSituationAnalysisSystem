package com.aicampus.controller;

import com.aicampus.model.PageResult;
import com.aicampus.model.RiskWarning;
import com.aicampus.model.Score;
import com.aicampus.service.RiskWarningService;
import com.aicampus.service.ScoreService;
import com.aicampus.session.UserSession;
import com.aicampus.util.AppExecutors;
import com.aicampus.util.ViewLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class StudentDashboardController {

    @FXML private Label valueAvgScore;
    @FXML private Label valueWarnings;
    @FXML private Label valueUsername;
    @FXML private TableView<Score> scoreTable;
    @FXML private TableColumn<Score, Integer> colCourseId;
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

        colCourseId.setCellValueFactory(new PropertyValueFactory<>("courseId"));
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
                PageResult<Score> scoreRes = ScoreService.getScorePage(1, 5);
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
                    valueWarnings.setText(String.valueOf(warnings.size()));
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
        });

        AppExecutors.submit(task::run);
    }

    @FXML private void goToScores(MouseEvent e) { navigateToWebView("/student/scores"); }
    @FXML private void goToAnalysis(MouseEvent e) { navigateToWebView("/student/analysis"); }
    @FXML private void goToDiagnosis(MouseEvent e) { navigateToWebView("/student/diagnosis"); }
    @FXML private void goToAdvice(MouseEvent e) { navigateToWebView("/student/advice"); }
    @FXML private void goToComment(MouseEvent e) { navigateToWebView("/student/comment"); }
    @FXML private void goToRisk(MouseEvent e) { navigateToWebView("/student/risk"); }

    private void navigateToWebView(String route) {
        javafx.scene.Node node = valueAvgScore;
        while (node != null) {
            if (node instanceof BorderPane) {
                BorderPane bp = (BorderPane) node;
                if (bp.getCenter() != null || bp.getLeft() == null) {
                    ViewLoader.loadWebView(bp, route);
                    return;
                }
            }
            node = node.getParent();
        }
    }
}
