package com.aicampus.controller;

import com.aicampus.model.ApiResponse;
import com.aicampus.service.ApiClient;
import com.aicampus.util.AppExecutors;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

import java.util.Map;

public class TeacherProfileController {

    @FXML private PasswordField oldPassword;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField confirmPassword;
    @FXML private Label passwordResultLabel;

    @FXML
    private void handleChangePassword() {
        String oldPwd = oldPassword.getText().trim();
        String newPwd = newPassword.getText().trim();
        String confirmPwd = confirmPassword.getText().trim();

        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            showResult("请填写所有密码字段", true);
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            showResult("两次输入的新密码不一致", true);
            return;
        }

        Map<String, String> body = Map.of(
                "oldPassword", oldPwd,
                "newPassword", newPwd
        );

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                return ApiClient.put("/auth/password", body, new TypeReference<ApiResponse<Void>>() {});
            }
        };
        task.setOnSucceeded(e -> {
            showResult("密码修改成功", false);
            oldPassword.clear();
            newPassword.clear();
            confirmPassword.clear();
        });
        task.setOnFailed(e -> showResult("密码修改失败: " + task.getException().getMessage(), true));
        AppExecutors.submit(task::run);
    }

    private void showResult(String msg, boolean isError) {
        passwordResultLabel.setText(msg);
        passwordResultLabel.setVisible(true);
        passwordResultLabel.setManaged(true);
        passwordResultLabel.getStyleClass().removeAll("result-success", "status-danger");
        passwordResultLabel.getStyleClass().add(isError ? "status-danger" : "result-success");
    }
}
