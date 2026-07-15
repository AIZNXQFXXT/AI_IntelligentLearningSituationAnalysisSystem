package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.service.AuthService;
import com.campus.client.util.AlertHelper;
import com.campus.client.util.FxmlLoader;
import com.campus.client.util.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainLayoutController implements Initializable {

    @FXML private VBox sidebar;
    @FXML private VBox menuContainer;
    @FXML private Label roleLabel;
    @FXML private Label usernameLabel;
    @FXML private Button logoutButton;
    @FXML private Label pageTitle;
    @FXML private StackPane contentArea;
    @FXML private Button profileButton;

    private final AuthService authService = new AuthService();
    private VBox lastSelectedMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SessionManager session = SessionManager.getInstance();
        roleLabel.setText(getRoleName(session.getRole()));
        usernameLabel.setText(session.getUsername());
        buildMenu();
    }

    private String getRoleName(String role) {
        if ("ADMIN".equals(role)) return "管理员";
        if ("TEACHER".equals(role)) return "教师";
        if ("STUDENT".equals(role)) return "学生";
        return role;
    }

    private void buildMenu() {
        menuContainer.getChildren().clear();
        SessionManager session = SessionManager.getInstance();

        if (session.isAdmin()) {
            addSectionTitle("系统管理");
            addMenuItem("仪表盘", "admin_dashboard.fxml", "首页概览");
            addMenuItem("用户管理", "user_management.fxml", "用户管理");
            addMenuItem("班级管理", "class_management.fxml", "班级管理");
            addMenuItem("教师管理", "teacher_management.fxml", "教师管理");
            addMenuItem("学生管理", "student_management.fxml", "学生管理");
            addMenuItem("课程管理", "course_management.fxml", "课程管理");
            addMenuItem("考试批次", "exam_management.fxml", "考试批次管理");
            addMenuItem("教学任务", "teaching_task_management.fxml", "教学任务管理");
            addMenuItem("成绩归档", "score_archive.fxml", "成绩归档");
            addMenuItem("系统配置", "system_config.fxml", "系统配置");
            addMenuItem("日志管理", "log_management.fxml", "日志管理");
        } else if (session.isTeacher()) {
            addSectionTitle("教学工作");
            addMenuItem("工作台", "teacher_dashboard.fxml", "教师工作台");
            addMenuItem("成绩管理", "teacher_scores.fxml", "成绩管理");
            addMenuItem("评语管理", "teacher_comments.fxml", "评语管理");
            addMenuItem("学业诊断", "teacher_diagnosis.fxml", "学业诊断");
            addMenuItem("预警管理", "teacher_risk.fxml", "学业预警管理");
        } else if (session.isStudent()) {
            addSectionTitle("我的学业");
            addMenuItem("学业概览", "student_dashboard.fxml", "我的学业概览");
            addMenuItem("我的成绩", "student_scores.fxml", "我的成绩");
        }

        if (!menuContainer.getChildren().isEmpty()) {
            for (var child : menuContainer.getChildren()) {
                if (child instanceof VBox vBox && vBox.getStyleClass().contains("sidebar-menu-item")) {
                    handleMenuClick(vBox);
                    break;
                }
            }
        }
    }

    private void addSectionTitle(String title) {
        VBox section = new VBox();
        section.getStyleClass().add("sidebar-section-title");
        Label label = new Label(title);
        section.getChildren().add(label);
        menuContainer.getChildren().add(section);
    }

    private void addMenuItem(String label, String fxml, String pageTitleText) {
        VBox item = new VBox();
        item.getStyleClass().add("sidebar-menu-item");
        item.setPadding(new Insets(12, 20, 12, 24));
        Label lbl = new Label(label);
        item.getChildren().add(lbl);
        item.setOnMouseClicked(e -> {
            handleMenuClick(item);
            loadContent(fxml, pageTitleText);
        });
        menuContainer.getChildren().add(item);
    }

    private void handleMenuClick(VBox selectedItem) {
        if (lastSelectedMenu != null) {
            lastSelectedMenu.getStyleClass().remove("active");
        }
        selectedItem.getStyleClass().add("active");
        lastSelectedMenu = selectedItem;
    }

    private void loadContent(String fxml, String title) {
        pageTitle.setText(title);
        try {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(FxmlLoader.load(fxml));
        } catch (IOException e) {
            AlertHelper.showError("加载失败", "无法加载页面: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        if (AlertHelper.showConfirm("确认退出", "确定要退出登录吗？")) {
            Task<Void> logoutTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    authService.logout();
                    return null;
                }
            };
            logoutTask.setOnSucceeded(e -> Platform.runLater(() -> {
                SessionManager.getInstance().clear();
                try {
                    StackPane root = (StackPane) sidebar.getScene().getRoot();
                    root.getChildren().clear();
                    root.getChildren().add(FxmlLoader.load("login.fxml"));
                } catch (IOException ex) {
                    AlertHelper.showError("错误", "退出失败");
                }
            }));
            new Thread(logoutTask).start();
        }
    }

    @FXML
    private void handleProfile() {
        TextInputDialog dialog = new TextInputDialog(SessionManager.getInstance().getUsername());
        dialog.setTitle("个人信息");
        dialog.setHeaderText("修改个人信息");
        dialog.setContentText("用户名:");
        dialog.show();
    }
}
