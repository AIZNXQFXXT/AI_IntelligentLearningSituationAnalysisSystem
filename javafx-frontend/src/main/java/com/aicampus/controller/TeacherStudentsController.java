package com.aicampus.controller;

import com.aicampus.model.Student;
import com.aicampus.service.StudentService;
import com.aicampus.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;

import java.util.List;

public class TeacherStudentsController {

    @FXML private TableView<Student> table;
    @FXML private TableColumn<Student, String> colStudentNo;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colGender;
    @FXML private TableColumn<Student, String> colPhone;
    @FXML private TableColumn<Student, Integer> colEnrollYear;
    @FXML private TableColumn<Student, Integer> colStatus;
    @FXML private TableColumn<Student, Void> colAction;

    private final ObservableList<Student> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colStudentNo.setCellValueFactory(new PropertyValueFactory<>("studentNo"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEnrollYear.setCellValueFactory(new PropertyValueFactory<>("enrollYear"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item == 1 ? "正常" : "停用");
                    label.getStyleClass().add(item == 1 ? "tag-success" : "tag-danger");
                    setGraphic(label);
                }
            }
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("查看画像");
            {
                btn.getStyleClass().addAll("btn-primary", "btn-sm");
                btn.setOnAction(e -> showPortrait(getTableView().getItems().get(getIndex())));
                HBox box = new HBox(btn); box.setAlignment(Pos.CENTER); setGraphic(box);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn.getParent());
            }
        });

        table.setItems(tableData);
        loadData();
    }

    private void loadData() {
        Task<List<Student>> task = new Task<>() {
            @Override
            protected List<Student> call() throws Exception {
                return StudentService.getPage(1, 200).getRecords();
            }
        };
        task.setOnSucceeded(e -> {
            tableData.clear();
            tableData.addAll(task.getValue());
        });
        task.setOnFailed(e -> CrudHelper.showError("加载失败"));
        new Thread(task).start();
    }

    private void showPortrait(Student s) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("学生画像");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new javafx.geometry.Insets(20));

        grid.add(new Label("学号:"), 0, 0);
        grid.add(new Label(s.getStudentNo()), 1, 0);
        grid.add(new Label("姓名:"), 0, 1);
        grid.add(new Label(s.getName()), 1, 1);
        grid.add(new Label("性别:"), 0, 2);
        grid.add(new Label(s.getGender()), 1, 2);
        grid.add(new Label("班级ID:"), 0, 3);
        grid.add(new Label(String.valueOf(s.getClassId())), 1, 3);
        grid.add(new Label("入学年份:"), 0, 4);
        grid.add(new Label(String.valueOf(s.getEnrollYear())), 1, 4);
        grid.add(new Label("电话:"), 0, 5);
        grid.add(new Label(s.getPhone()), 1, 5);
        grid.add(new Label("监护人电话:"), 0, 6);
        grid.add(new Label(s.getGuardianPhone()), 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }
}
