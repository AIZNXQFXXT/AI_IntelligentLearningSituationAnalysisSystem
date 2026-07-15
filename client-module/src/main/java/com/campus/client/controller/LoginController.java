package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.service.AuthService;
import com.campus.client.util.AlertHelper;
import com.campus.client.util.FxmlLoader;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final AuthService authService = new AuthService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleComboBox.setItems(FXCollections.observableArrayList("管理员", "教师", "学生"));
        roleComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("请输入用户名和密码");
            return;
        }

        setLoading(true);

        Task<ApiResult<java.util.Map<String, Object>>> loginTask = new Task<>() {
            @Override
            protected ApiResult<java.util.Map<String, Object>> call() throws Exception {
                return authService.login(username, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            setLoading(false);
            ApiResult<java.util.Map<String, Object>> result = loginTask.getValue();
            if (result.isSuccess()) {
                navigateToMainLayout();
            } else {
                showError(result.getMessage() != null ? result.getMessage() : "登录失败");
            }
        });

        loginTask.setOnFailed(e -> {
            setLoading(false);
            showError("网络连接失败，请检查服务器");
        });

        new Thread(loginTask).start();
    }

    private void navigateToMainLayout() {
        try {
            StackPane root = (StackPane) loginButton.getScene().getRoot();
            root.getChildren().clear();
            root.getChildren().add(FxmlLoader.load("main_layout.fxml"));
        } catch (Exception ex) {
            showError("加载主界面失败: " + ex.getMessage());
        }
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        loadingIndicator.setVisible(loading);
        loadingIndicator.setManaged(loading);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
