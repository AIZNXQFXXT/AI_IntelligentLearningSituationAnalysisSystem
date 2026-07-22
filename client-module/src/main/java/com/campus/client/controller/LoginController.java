package com.aicampus.controller;

import com.aicampus.model.LoginResult;
import com.aicampus.service.AuthService;
import com.aicampus.session.UserSession;
import com.aicampus.util.AppExecutors;
import com.aicampus.util.ViewLoader;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty()) {
            showError("请输入用户名");
            return;
        }
        if (password.isEmpty()) {
            showError("请输入密码");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("登录中...");
        hideError();

        Task<LoginResult> loginTask = new Task<>() {
            @Override
            protected LoginResult call() throws Exception {
                return AuthService.login(username, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            LoginResult result = loginTask.getValue();
            UserSession.getInstance().login(result);
            navigateToDashboard(result.getRole());
        });

        loginTask.setOnFailed(event -> {
            Throwable ex = loginTask.getException();
            if (ex != null && ex.getMessage() != null) {
                showError(ex.getMessage());
            } else {
                showError("登录失败，请检查网络连接");
            }
            loginButton.setDisable(false);
            loginButton.setText("登 录");
        });

        AppExecutors.submit(loginTask::run);
    }

    @FXML
    private void onPasswordKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }

    private void navigateToDashboard(String role) {
        Platform.runLater(() -> {
            ViewLoader.loadScene("/fxml/MainLayout.fxml", "AI智能校园学情分析系统");
        });
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        });
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
