package com.campus.client.controller;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.Student;
import com.campus.client.service.ApiClient;
import com.campus.client.service.StudentService;
import com.campus.client.session.UserSession;
import com.campus.client.util.AppExecutors;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Map;

public class StudentProfileController {

    @FXML private Label valueStudentNo;
    @FXML private Label valueName;
    @FXML private Label valueGender;
    @FXML private Label valueClassName;
    @FXML private Label valueEnrollYear;
    @FXML private Label valueStatus;
    @FXML private TextField phoneField;
    @FXML private TextField guardianPhoneField;
    @FXML private Label editResultLabel;
    @FXML private PasswordField oldPassword;
    @FXML private PasswordField newPassword;
    @FXML private PasswordField confirmPassword;
    @FXML private Label passwordResultLabel;

    private Student currentStudent;

    @FXML
    public void initialize() {
        loadProfile();
    }

    private void loadProfile() {
        Task<Student> task = new Task<>() {
            @Override
            protected Student call() throws Exception {
                return ApiClient.get("/my/profile", new TypeReference<ApiResponse<Student>>() {});
            }
        };
        task.setOnSucceeded(e -> {
            currentStudent = task.getValue();
            if (currentStudent != null) {
                valueStudentNo.setText(currentStudent.getStudentNo() != null ? currentStudent.getStudentNo() : "--");
                valueName.setText(currentStudent.getName() != null ? currentStudent.getName() : "--");
                valueGender.setText(currentStudent.getGender() != null ? currentStudent.getGender() : "--");
                valueClassName.setText(currentStudent.getClassName() != null ? currentStudent.getClassName() : "--");
                valueEnrollYear.setText(String.valueOf(currentStudent.getEnrollYear()));
                valueStatus.setText(currentStudent.getStatus() == 1 ? "在读" : currentStudent.getStatus() == 0 ? "休学" : "退学");
                phoneField.setText(currentStudent.getPhone() != null ? currentStudent.getPhone() : "");
                guardianPhoneField.setText(currentStudent.getGuardianPhone() != null ? currentStudent.getGuardianPhone() : "");
            }
        });
        task.setOnFailed(e -> {
            showResult(editResultLabel, "加载个人信息失败", true);
        });
        AppExecutors.submit(task::run);
    }

    @FXML
    private void handleSave() {
        String phone = phoneField.getText().trim();
        String guardianPhone = guardianPhoneField.getText().trim();

        Map<String, String> body = Map.of(
                "phone", phone,
                "guardianPhone", guardianPhone
        );

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                return ApiClient.patch("/auth/profile", body, new TypeReference<ApiResponse<Void>>() {});
            }
        };
        task.setOnSucceeded(e -> {
            showResult(editResultLabel, "保存成功", false);
            loadProfile();
        });
        task.setOnFailed(e -> showResult(editResultLabel, "保存失败: " + task.getException().getMessage(), true));
        AppExecutors.submit(task::run);
    }

    @FXML
    private void handleChangePassword() {
        String oldPwd = oldPassword.getText().trim();
        String newPwd = newPassword.getText().trim();
        String confirmPwd = confirmPassword.getText().trim();

        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            showResult(passwordResultLabel, "请填写所有密码字段", true);
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            showResult(passwordResultLabel, "两次输入的新密码不一致", true);
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
            showResult(passwordResultLabel, "密码修改成功", false);
            oldPassword.clear();
            newPassword.clear();
            confirmPassword.clear();
        });
        task.setOnFailed(e -> showResult(passwordResultLabel, "密码修改失败: " + task.getException().getMessage(), true));
        AppExecutors.submit(task::run);
    }

    private void showResult(Label label, String msg, boolean isError) {
        label.setText(msg);
        label.setVisible(true);
        label.setManaged(true);
        label.getStyleClass().removeAll("result-success", "status-danger");
        label.getStyleClass().add(isError ? "status-danger" : "result-success");
    }
}
