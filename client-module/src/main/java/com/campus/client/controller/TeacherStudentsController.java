package com.campus.client.controller;

import com.campus.client.model.ClassInfo;
import com.campus.client.model.PageResult;
import com.campus.client.model.Student;
import com.campus.client.service.ClassService;
import com.campus.client.service.StudentService;
import com.campus.client.util.AppExecutors;
import com.campus.client.util.CrudHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.Collections;
import java.util.List;

public class TeacherStudentsController {

    @FXML private Label titleLabel;
    @FXML private ComboBox<ClassInfo> classComboBox;
    @FXML private TextField searchField;
    @FXML private TableView<Student> table;
    @FXML private TableColumn<Student, String> colStudentNo;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colGender;
    @FXML private TableColumn<Student, String> colPhone;
    @FXML private TableColumn<Student, Integer> colEnrollYear;
    @FXML private TableColumn<Student, Integer> colStatus;
    @FXML private TableColumn<Student, Void> colAction;
    @FXML private Pagination pagination;

    private final ObservableList<Student> tableData = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 20;
    private int totalItems = 0;

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
                    String text;
                    String styleClass;
                    switch (item) {
                        case 1:  text = "在读"; styleClass = "tag-success"; break;
                        case 0:  text = "休学"; styleClass = "tag-warning"; break;
                        case 2:  text = "退学"; styleClass = "tag-danger"; break;
                        default: text = "未知"; styleClass = "tag-danger"; break;
                    }
                    Label label = new Label(text);
                    label.getStyleClass().add(styleClass);
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

        pagination.setPageFactory(this::buildPage);

        // ComboBox 显示: 复用 ClassInfo.toString() = "grade className"
        classComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(ClassInfo c) { return c == null ? "" : c.toString(); }
            @Override public ClassInfo fromString(String s) { return null; }
        });
        // 用 valueProperty 监听而非 setOnAction: 程序化 selectFirst() 与用户选择都可靠触发
        classComboBox.valueProperty().addListener((obs, old, newVal) -> {
            currentPage = 1;
            pagination.setCurrentPageIndex(0);
            loadData();
        });

        loadMyClasses();
    }

    /** 加载本班班级列表 (班主任 ∪ 教学任务), 默认选中第一个 */
    private void loadMyClasses() {
        Task<PageResult<ClassInfo>> loadClasses = new Task<>() {
            @Override protected PageResult<ClassInfo> call() throws Exception {
                return ClassService.getMyClasses();
            }
        };
        loadClasses.setOnSucceeded(ev -> {
            PageResult<ClassInfo> pr = loadClasses.getValue();
            List<ClassInfo> list = pr != null && pr.getRecords() != null
                    ? pr.getRecords() : Collections.emptyList();
            classComboBox.getItems().setAll(list);
            if (!list.isEmpty()) {
                // selectFirst 会触发 valueProperty 监听 → loadData()
                classComboBox.getSelectionModel().selectFirst();
            } else {
                titleLabel.setText("本班学生 · 暂无所带班级");
                tableData.clear();
            }
        });
        loadClasses.setOnFailed(ev -> {
            titleLabel.setText("本班学生 · 加载失败");
            CrudHelper.showError("班级加载失败");
        });
        AppExecutors.submit(loadClasses::run);
    }

    @FXML
    private void handleSearch() {
        currentPage = 1;
        pagination.setCurrentPageIndex(0);
        loadData();
    }

    private Label buildPage(int pageIndex) {
        int targetPage = pageIndex + 1;
        if (targetPage != currentPage) {
            currentPage = targetPage;
            loadData();
        }
        return new Label("");
    }

    private void loadData() {
        ClassInfo selected = classComboBox.getValue();
        if (selected == null) {
            tableData.clear();
            return;
        }
        // 动态标题
        titleLabel.setText("本班学生 · " + selected.toString());

        final Integer classId = selected.getId();
        final String keyword = searchField != null && searchField.getText() != null
                ? searchField.getText().trim() : "";
        String cn = selected.getClassName();
        Task<PageResult<Student>> task = new Task<>() {
            @Override
            protected PageResult<Student> call() throws Exception {
                String kw = keyword.isEmpty() ? null : keyword;
                return StudentService.getPage(currentPage, pageSize, kw, classId);
            }
        };
        task.setOnSucceeded(e -> {
            PageResult<Student> result = task.getValue();
            tableData.clear();
            if (result != null && result.getRecords() != null) {
                for (Student s : result.getRecords()) {
                    s.setClassName(cn);
                }
                tableData.addAll(result.getRecords());
            }
            totalItems = result != null ? result.getTotal() : 0;
            int pageCount = (int) Math.ceil((double) totalItems / pageSize);
            if (pageCount < 1) pageCount = 1;
            pagination.setPageCount(pageCount);
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            CrudHelper.showError(ex != null && ex.getMessage() != null ? ex.getMessage() : "加载失败");
        });
        AppExecutors.submit(task::run);
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
        grid.add(new Label("班级:"), 0, 3);
        grid.add(new Label(s.getClassName() != null ? s.getClassName() : String.valueOf(s.getClassId())), 1, 3);
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
