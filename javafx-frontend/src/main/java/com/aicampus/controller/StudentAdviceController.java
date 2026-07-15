package com.aicampus.controller;

import com.aicampus.model.AiDiagnosis;
import com.aicampus.service.DiagnosisService;
import com.aicampus.util.AppExecutors;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
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

    @FXML
    public void initialize() {
        loadLatestDiagnosis();
    }

    private void loadLatestDiagnosis() {
        Task<List<AiDiagnosis>> task = new Task<>() {
            @Override
            protected List<AiDiagnosis> call() throws Exception {
                return DiagnosisService.getPage(1, 1, null).getRecords();
            }
        };
        task.setOnSucceeded(e -> {
            List<AiDiagnosis> list = task.getValue();
            if (list.isEmpty()) {
                emptyLabel.setVisible(true);
                contentBox.setVisible(false);
            } else {
                AiDiagnosis d = list.get(0);
                diagnosisText.setText(d.getDiagnosisText());
                strengths.setText(d.getStrengths());
                weaknesses.setText(d.getWeaknesses());
                trendAnalysis.setText(d.getTrendAnalysis());
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
