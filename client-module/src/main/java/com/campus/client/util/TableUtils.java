package com.aicampus.util;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;
import java.util.function.Function;

public class TableUtils {

    public static <T> void setupActionColumn(TableColumn<T, Void> column,
                                              Consumer<T> onEdit, Consumer<T> onDelete) {
        column.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button deleteBtn = new Button("删除");
            {
                editBtn.getStyleClass().addAll("btn-edit", "btn-sm");
                deleteBtn.getStyleClass().addAll("btn-delete", "btn-sm");
                editBtn.setOnAction(e -> onEdit.accept(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> onDelete.accept(getTableView().getItems().get(getIndex())));
                HBox box = new HBox(8, editBtn, deleteBtn);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn.getParent());
            }
        });
    }

    public static <T> void setupActionColumn(TableColumn<T, Void> column,
                                              Consumer<T> onEdit, Consumer<T> onDelete,
                                              Consumer<T> onStatusToggle, Function<T, String> statusTextGetter) {
        column.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button deleteBtn = new Button("删除");
            private final Button statusBtn = new Button();
            {
                editBtn.getStyleClass().addAll("btn-edit", "btn-sm");
                deleteBtn.getStyleClass().addAll("btn-delete", "btn-sm");
                statusBtn.getStyleClass().addAll("btn-warning", "btn-sm");

                editBtn.setOnAction(e -> onEdit.accept(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> onDelete.accept(getTableView().getItems().get(getIndex())));
                statusBtn.setOnAction(e -> onStatusToggle.accept(getTableView().getItems().get(getIndex())));

                HBox box = new HBox(8, editBtn, deleteBtn, statusBtn);
                box.setAlignment(Pos.CENTER);
                setGraphic(box);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    T currentItem = getTableView().getItems().get(getIndex());
                    statusBtn.setText(statusTextGetter.apply(currentItem));
                    setGraphic(editBtn.getParent());
                }
            }
        });
    }

    public static <S> void setupRiskLevelCell(TableColumn<S, String> column) {
        column.setCellFactory(param -> new TableCell<S, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label label = new Label(item);
                switch (item) {
                    case "HIGH": label.getStyleClass().add("tag-danger"); break;
                    case "MEDIUM": label.getStyleClass().add("tag-warning"); break;
                    default: label.getStyleClass().add("tag-success"); break;
                }
                setGraphic(label);
            }
        });
    }

    public static <S> void setupStatusCell(TableColumn<S, String> column) {
        column.setCellFactory(param -> new TableCell<S, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label label = new Label("HANDLED".equals(item) ? "已处理" : "未处理");
                label.getStyleClass().add("HANDLED".equals(item) ? "tag-success" : "tag-danger");
                setGraphic(label);
            }
        });
    }
}
