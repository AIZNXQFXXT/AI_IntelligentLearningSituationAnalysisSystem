package com.campus.client.controller;

import com.campus.client.model.Course;
import com.campus.client.model.Score;
import com.campus.client.service.CourseService;
import com.campus.client.service.ScoreService;
import com.campus.client.service.SemesterService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentScoresController {

    @FXML private TableView<Score> table;
    @FXML private TableColumn<Score, String> colCourseId;
    @FXML private TableColumn<Score, Double> colRegular;
    @FXML private TableColumn<Score, Double> colExam;
    @FXML private TableColumn<Score, Double> colFinal;
    @FXML private TableColumn<Score, Integer> colRankClass;
    @FXML private TableColumn<Score, Integer> colRankGrade;
    @FXML private TableColumn<Score, String> colAudit;
    @FXML private ComboBox<String> semesterFilter;
    @FXML private Label emptyLabel;

    private final ObservableList<Score> tableData = FXCollections.observableArrayList();
    private final Map<Integer, String> courseMap = new HashMap<>();

    @FXML
    public void initialize() {
        semesterFilter.setOnAction(e -> loadData());
        loadSemesters();

        colCourseId.setCellValueFactory(data ->
                new SimpleStringProperty(courseMap.getOrDefault(data.getValue().getCourseId(), "未知")));
        colRegular.setCellValueFactory(new PropertyValueFactory<>("regularScore"));
        colExam.setCellValueFactory(new PropertyValueFactory<>("examScore"));
        colFinal.setCellValueFactory(new PropertyValueFactory<>("finalScore"));
        colRankClass.setCellValueFactory(new PropertyValueFactory<>("rankClass"));
        colRankGrade.setCellValueFactory(new PropertyValueFactory<>("rankGrade"));
        colAudit.setCellValueFactory(new PropertyValueFactory<>("auditStatus"));

        colFinal.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(String.format("%.1f", item));
                    label.setStyle("-fx-font-weight: bold;");
                    setGraphic(label);
                }
            }
        });

        colAudit.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label("ARCHIVED".equals(item) ? "已审核" : "待审核");
                    label.getStyleClass().add("ARCHIVED".equals(item) ? "tag-success" : "tag-info");
                    setGraphic(label);
                }
            }
        });

        table.setItems(tableData);
        loadMaps();
        loadData();
    }

    private void loadSemesters() {
        Task<List<String>> task = new Task<>() {
            @Override protected List<String> call() throws Exception {
                return SemesterService.getAllSemesters();
            }
        };
        task.setOnSucceeded(e -> {
            semesterFilter.setItems(FXCollections.observableArrayList(task.getValue()));
            semesterFilter.getSelectionModel().selectFirst();
        });
        task.setOnFailed(e -> {
            semesterFilter.setItems(FXCollections.observableArrayList("2025-1", "2024-2", "2024-1"));
            semesterFilter.getSelectionModel().selectFirst();
        });
        AppExecutors.submit(task::run);
    }

    private void loadMaps() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (Course c : CourseService.getAll())
                    courseMap.put(c.getId(), c.getName());
                return null;
            }
        };
        task.setOnSucceeded(e -> table.refresh());
        task.setOnFailed(e -> {});
        AppExecutors.submit(task::run);
    }

    private void loadData() {
        String semester = semesterFilter.getValue();
        Task<List<Score>> task = new Task<>() {
            @Override
            protected List<Score> call() throws Exception {
                return ScoreService.getMyScores(1, 200, semester).getRecords();
            }
        };
        task.setOnSucceeded(e -> {
            List<Score> scores = task.getValue();
            tableData.clear();
            tableData.addAll(scores);
            emptyLabel.setVisible(scores.isEmpty());
            emptyLabel.setManaged(scores.isEmpty());
            table.setVisible(!scores.isEmpty());
            table.setManaged(!scores.isEmpty());
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
    }
}
