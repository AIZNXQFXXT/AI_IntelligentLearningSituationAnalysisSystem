package com.campus.client.controller;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.ImportProgress;
import com.campus.client.model.TaskResult;
import com.campus.client.service.ApiClient;
import com.campus.client.util.AppExecutors;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImportProgressController {

    @FXML private ProgressBar progressBar;
    @FXML private Label percentLabel;
    @FXML private Label statusLabel;
    @FXML private Label processedLabel;
    @FXML private Label totalLabel;
    @FXML private Label successLabel;
    @FXML private Label failLabel;
    @FXML private TextArea errorArea;
    @FXML private Button closeButton;

    private Stage stage;
    private Long taskId;
    private Runnable onComplete;
    private final AtomicBoolean polling = new AtomicBoolean(true);
    private volatile boolean complete = false;

    public static void show(Long taskId, Runnable onComplete) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ImportProgressController.class.getResource("/fxml/ImportProgressView.fxml"));
            Scene scene = new Scene(loader.load());
            ImportProgressController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("导入进度");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.setOnCloseRequest(e -> {
                if (!controller.complete) {
                    e.consume();
                } else {
                    if (controller.onComplete != null) controller.onComplete.run();
                }
            });

            controller.stage = stage;
            controller.onComplete = onComplete;
            controller.startPolling(taskId);
            stage.showAndWait();
        } catch (Exception e) {
            com.campus.client.util.CrudHelper.showError("打开进度窗口失败: " + e.getMessage());
        }
    }

    public void initData(Long taskId, Runnable onComplete) {
        this.taskId = taskId;
        this.onComplete = onComplete;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public boolean isComplete() {
        return complete;
    }

    private void startPolling(Long taskId) {
        statusLabel.setText("正在处理...");
        AppExecutors.submit(() -> {
            while (polling.get() && !complete) {
                try {
                    ImportProgress progress = ApiClient.get("/tasks/" + taskId,
                            new TypeReference<ApiResponse<ImportProgress>>() {});
                    if (progress != null) {
                        String status = progress.getStatus();
                        int pct = progress.getProgress();
                        int current = progress.getCurrentCount();
                        int total = progress.getTotalCount();

                        Platform.runLater(() -> updateProgress(pct, current, total));

                        if ("COMPLETED".equals(status)) {
                            polling.set(false);
                            loadResult(taskId);
                            return;
                        }
                        if ("FAILED".equals(status)) {
                            polling.set(false);
                            complete = true;
                            Platform.runLater(() -> {
                                statusLabel.setText("导入失败");
                                percentLabel.setText("失败");
                                percentLabel.getStyleClass().remove("progress-percent");
                                percentLabel.getStyleClass().add("text-danger");
                                closeButton.setDisable(false);
                            });
                            return;
                        }
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("查询进度失败: " + e.getMessage()));
                    try { Thread.sleep(3000); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });
    }

    private void updateProgress(int pct, int current, int total) {
        progressBar.setProgress(pct / 100.0);
        percentLabel.setText(pct + "%");
        processedLabel.setText(String.valueOf(current));
        totalLabel.setText(String.valueOf(total));
    }

    private void loadResult(Long taskId) {
        try {
            TaskResult result = ApiClient.get("/tasks/" + taskId + "/result",
                    new TypeReference<ApiResponse<TaskResult>>() {});
            if (result != null) {
                complete = true;
                Platform.runLater(() -> {
                    updateProgress(100, result.getSuccess() + result.getFailed(),
                            result.getSuccess() + result.getFailed());
                    statusLabel.setText("导入完成");
                    percentLabel.setText("完成");
                    percentLabel.getStyleClass().remove("progress-percent");
                    percentLabel.getStyleClass().add("text-success");
                    successLabel.setText(String.valueOf(result.getSuccess()));
                    failLabel.setText(String.valueOf(result.getFailed()));

                    List<Map<String, Object>> errors = result.getErrors();
                    if (errors != null && !errors.isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        for (Map<String, Object> err : errors) {
                            sb.append("第 ").append(err.get("row")).append(" 行: ")
                              .append(err.get("reason")).append("\n");
                        }
                        errorArea.setText(sb.toString());
                        errorArea.setVisible(true);
                        errorArea.setManaged(true);
                        if (stage != null) stage.sizeToScene();
                    }
                    closeButton.setDisable(false);
                });
            }
        } catch (Exception e) {
            complete = true;
            Platform.runLater(() -> {
                statusLabel.setText("获取结果失败: " + e.getMessage());
                closeButton.setDisable(false);
            });
        }
    }

    @FXML
    private void handleClose() {
        polling.set(false);
        complete = true;
        if (stage != null) stage.close();
        if (onComplete != null) onComplete.run();
    }
}
