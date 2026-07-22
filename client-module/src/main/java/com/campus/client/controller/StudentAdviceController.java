package com.campus.client.controller;

import com.campus.client.model.AiDiagnosis;
import com.campus.client.model.DiagnosisReport;
import com.campus.client.service.DiagnosisService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import com.campus.client.util.JsonFormatter;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.List;

public class StudentAdviceController {

    @FXML private Label emptyLabel;
    @FXML private VBox contentBox;
    @FXML private TextArea diagnosisText;
    @FXML private TextArea strengths;
    @FXML private TextArea weaknesses;
    @FXML private TextArea trendAnalysis;
    @FXML private TextArea suggestionsArea;
    @FXML private Label riskLevelLabel;

    @FXML
    public void initialize() {
        loadLatestDiagnosis();
    }

    private void loadLatestDiagnosis() {
        Task<List<AiDiagnosis>> task = new Task<>() {
            @Override
            protected List<AiDiagnosis> call() throws Exception {
                return DiagnosisService.getMyPage(1, 1).getRecords();
            }
        };
        task.setOnSucceeded(e -> {
            List<AiDiagnosis> list = task.getValue();
            if (list.isEmpty()) {
                emptyLabel.setVisible(true);
                contentBox.setVisible(false);
            } else {
                AiDiagnosis d = list.get(0);
                DiagnosisReport report = JsonFormatter.parseDiagnosisReport(d.getDiagnosisText());
                diagnosisText.setText(report.getOverall() != null ? report.getOverall() : d.getDiagnosisText());
                strengths.setText(JsonFormatter.formatKeyValueList(report.getStrengths(), "科目", "描述"));
                weaknesses.setText(JsonFormatter.formatKeyValueList(report.getWeaknesses(), "科目", "描述"));
                trendAnalysis.setText(report.getTrend() != null ? report.getTrend() : d.getTrend());
                suggestionsArea.setText(JsonFormatter.formatNumberedList(report.getSuggestions()));
                String riskLevel = report.getRiskLevel();
                if (riskLevel != null) {
                    riskLevelLabel.setText(JsonFormatter.formatRiskLevel(riskLevel));
                    riskLevelLabel.getStyleClass().clear();
                    riskLevelLabel.getStyleClass().add("tag-" + switch (riskLevel.toUpperCase()) {
                        case "HIGH" -> "danger";
                        case "MEDIUM" -> "warning";
                        default -> "success";
                    });
                }
                contentBox.setVisible(true);
            }
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }
}
