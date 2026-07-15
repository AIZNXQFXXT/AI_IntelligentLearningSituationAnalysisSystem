package com.campus.client.controller;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.User;
import com.campus.client.service.UserService;
import com.campus.client.util.AlertHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class UserController implements Initializable {

    @FXML private ComboBox<String> roleFilter;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Long> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colRealName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, String> colCreateTime;
    @FXML private TableColumn<User, Void> colActions;
    @FXML private Label totalLabel;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;

    private final UserService userService = new UserService();
    private int currentPage = 0;
    private int totalPages = 1;
    private final ObservableList<User> userData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleFilter.setItems(FXCollections.observableArrayList("全部", "ADMIN", "TEACHER", "STUDENT"));
        roleFilter.getSelectionModel().selectFirst();
        setupTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRealName.setCellValueFactory(new PropertyValueFactory<>("realName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colStatus.setCellValueFactory(cellData -> {
            Integer status = cellData.getValue().getStatus();
            return new SimpleStringProperty(status != null && status == 1 ? "启用" : "禁用");
        });
        colCreateTime.setCellValueFactory(new PropertyValueFactory<>("createTime"));
        colActions.setCellFactory(col -> new TableCell<>() {
            {
                Button editBtn = new Button("编辑");
                editBtn.setStyle("-fx-background-color: #1E88E5; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;");
                Button disableBtn = new Button("禁用");
                disableBtn.setStyle("-fx-background-color: #F57F17; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;");
                Button resetBtn = new Button("重置密码");
                resetBtn.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 2 8;");

                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                disableBtn.setOnAction(e -> handleToggleStatus(getTableView().getItems().get(getIndex())));
                resetBtn.setOnAction(e -> handleResetPassword(getTableView().getItems().get(getIndex())));

                setGraphic(new javafx.scene.layout.HBox(4, editBtn, disableBtn, resetBtn));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : getGraphic());
            }
        });
        userTable.setItems(userData);
    }

    @FXML
    private void handleSearch() {
        currentPage = 0;
        loadData();
    }

    private void loadData() {
        String role = roleFilter.getValue();
        if ("全部".equals(role)) role = null;

        String finalRole = role;
        Task<ApiResult<PageResult<User>>> task = new Task<>() {
            @Override
            protected ApiResult<PageResult<User>> call() throws Exception {
                return userService.getUsers(currentPage, 15, finalRole);
            }
        };
        task.setOnSucceeded(e -> {
            ApiResult<PageResult<User>> result = task.getValue();
            if (result.isSuccess() && result.getData() != null) {
                PageResult<User> page = result.getData();
                userData.setAll(page.getContent());
                totalPages = Math.max(1, page.getTotalPages());
                pageInfo.setText("第 " + (currentPage + 1) + " 页 / 共 " + totalPages + " 页");
                totalLabel.setText("共 " + page.getTotalElements() + " 条");
            }
        });
        new Thread(task).start();
    }

    private void handleEdit(User user) {
        TextInputDialog dialog = new TextInputDialog(user.getRealName());
        dialog.setTitle("编辑用户");
        dialog.setHeaderText("修改用户: " + user.getUsername());
        dialog.setContentText("姓名:");
        dialog.showAndWait().ifPresent(name -> {
            user.setRealName(name);
            Task<Void> t = new Task<>() {
                @Override protected Void call() throws Exception { return null; }
            };
            new Thread(t).start();
        });
    }

    private void handleToggleStatus(User user) {
        Integer newStatus = user.getStatus() != null && user.getStatus() == 1 ? 0 : 1;
        String action = newStatus == 1 ? "启用" : "禁用";
        if (AlertHelper.showConfirm("确认", "确定要" + action + "用户 " + user.getUsername() + " 吗？")) {
            Task<Void> t = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    userService.updateUserStatus(user.getId(), newStatus);
                    return null;
                }
            };
            t.setOnSucceeded(e -> {
                AlertHelper.showInfo("成功", "操作成功");
                loadData();
            });
            new Thread(t).start();
        }
    }

    private void handleResetPassword(User user) {
        if (AlertHelper.showConfirm("确认", "重置用户 " + user.getUsername() + " 的密码为默认密码？")) {
            Task<Void> t = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    userService.resetPassword(user.getId(), "123456");
                    return null;
                }
            };
            t.setOnSucceeded(e -> AlertHelper.showInfo("成功", "密码已重置"));
            new Thread(t).start();
        }
    }

    @FXML
    private void handleAdd() {
        AlertHelper.showInfo("新增用户", "请在系统中通过管理员接口创建用户");
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            loadData();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadData();
        }
    }
}
