package com.aicampus;

import com.aicampus.session.UserSession;
import com.aicampus.util.ViewLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Stage primaryStage;
    private static final String CSS_PATH = "/css/styles.css";

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setTitle("AI智能校园学情分析系统");

        String fxmlPath;
        if (UserSession.getInstance().isLoggedIn()) {
            fxmlPath = "/fxml/MainLayout.fxml";
        } else {
            fxmlPath = "/fxml/LoginView.fxml";
        }

        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static String getCssPath() {
        return CSS_PATH;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
