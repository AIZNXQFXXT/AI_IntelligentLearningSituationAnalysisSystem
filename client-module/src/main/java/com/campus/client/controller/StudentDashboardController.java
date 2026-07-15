package com.campus.client.controller;

import com.campus.client.model.*;
import com.campus.client.service.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class StudentDashboardController implements Initializable {

    @FXML private Label courseCount;
    @FXML private Label avgScore;
    @FXML private Label warningCount;
    @FXML private TableView<Score> recentScoreTable;
    @FXML private TableColumn<Score, String> colCourseName;
    @FXML private TableColumn<Score, String> colExamName;
    @FXML private TableColumn<Score, Double> colScore;
    @FXML private TableColumn<Score, Double> colRank;
    @FXML private VBox suggestionList;
    @FXML private VBox warningList;

    private final ScoreService scoreService = new ScoreService();
    private final SuggestionService suggestionService = new SuggestionService();
    private final RiskWarningService warningService = new RiskWarningService();
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
        recentScoreTable.setItems(scoreData);
    }

    private void loadData() {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                try {
                    ApiResult<PageResult<Score>> scores = scoreService.getScores(0, 10, null, null);
                    if (scores.isSuccess() && scores.getData() != null) {
                        Platform.runLater(() -> {
                            scoreData.setAll(scores.getData().getContent());
                            courseCount.setText(String.valueOf(scores.getData().getTotalElements()));
                        });
                    }
                } catch (Exception e) { }

                try {
                    ApiResult<List<Suggestion>> suggestions = suggestionService.getMySuggestions();
                    if (suggestions.isSuccess() && suggestions.getData() != null) {
                        Platform.runLater(() -> {
                            suggestionList.getChildren().clear();
                            for (Suggestion s : suggestions.getData()) {
                                Label lbl = new Label("- " + s.getContent());
                                lbl.setWrapText(true);
                                lbl.setPadding(new Insets(4));
                                suggestionList.getChildren().add(lbl);
                            }
                        });
                    }
                } catch (Exception e) { }

                try {
                    ApiResult<List<RiskWarning>> warnings = warningService.getMyWarnings();
                    if (warnings.isSuccess() && warnings.getData() != null) {
                        Platform.runLater(() -> {
                            warningList.getChildren().clear();
                            warningCount.setText(String.valueOf(warnings.getData().size()));
                            for (RiskWarning w : warnings.getData()) {
                                Label lbl = new Label("- [" + w.getLevel() + "] " + w.getContent());
                                lbl.setWrapText(true);
                                lbl.setPadding(new Insets(4));
                                warningList.getChildren().add(lbl);
                            }
                        });
                    }
                } catch (Exception e) { }

                return null;
            }
        };
        new Thread(task).start();
    }
}
