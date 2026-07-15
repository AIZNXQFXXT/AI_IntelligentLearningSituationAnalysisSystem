package com.campus.client;

import com.campus.client.util.FxmlLoader;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CampusApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FxmlLoader.load("login.fxml");
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        primaryStage.setTitle("AI Campus - 校园学业智能管理系统");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
