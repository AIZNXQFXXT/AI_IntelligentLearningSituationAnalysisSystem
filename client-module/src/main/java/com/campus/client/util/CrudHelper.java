package com.aicampus.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.function.Consumer;

public class CrudHelper {

    public static <T> void confirmDelete(Consumer<T> onConfirm, T item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确认删除此记录?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            onConfirm.accept(item);
        }
    }

    public static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static <T> void runAsync(java.util.function.Supplier<T> operation,
                                     Consumer<T> onSuccess,
                                     String errorMessage) {
        javafx.concurrent.Task<T> task = new javafx.concurrent.Task<>() {
            @Override
            protected T call() throws Exception {
                return operation.get();
            }
        };
        task.setOnSucceeded(e -> {
            T result = task.getValue();
            if (result != null) onSuccess.accept(result);
        });
        task.setOnFailed(e -> showError(errorMessage));
        AppExecutors.submit(task::run);
    }

    public static void runAsyncVoid(Runnable operation,
                                     Runnable onSuccess,
                                     String errorMessage) {
        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                operation.run();
                return null;
            }
        };
        task.setOnSucceeded(e -> onSuccess.run());
        task.setOnFailed(e -> showError(errorMessage));
        AppExecutors.submit(task::run);
    }
}
