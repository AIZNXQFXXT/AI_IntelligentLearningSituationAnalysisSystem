package com.campus.client;

import com.campus.client.session.UserSession;
import com.campus.client.util.AppExecutors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.InetSocketAddress;
import java.net.Socket;

public class App extends Application {

    private static Stage primaryStage;
    private static final String CSS_PATH = "/css/styles.css";

    private Label statusLabel;
    private ProgressBar progressBar;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("AI智能校园学情分析系统");
        showSplash(stage);
    }

    private void showSplash(Stage stage) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 0% 100%, #667eea 0%, #764ba2 100%);");

        Label title = new Label("AI智能校园学情分析系统");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("正在连接服务器，请稍候...");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.8);");

        progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: white;");

        statusLabel = new Label("正在连接...");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.6);");

        root.getChildren().addAll(title, subtitle, progressBar, statusLabel);

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.show();

        pollBackend(stage);
    }

    private void pollBackend(Stage stage) {
        AppExecutors.supplyAsync(() -> {
            for (int i = 0; i < 30; i++) {
                if (isBackendReady()) {
                    return true;
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return false;
                }
            }
            return false;
        }).thenAccept(ready -> Platform.runLater(() -> {
            if (ready) {
                loadMainView();
            } else {
                statusLabel.setText("连接服务器失败，请检查后端是否已启动");
                progressBar.setVisible(false);
            }
        }));
    }

    private boolean isBackendReady() {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("localhost", 8080), 2000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void loadMainView() {
        try {
            String fxmlPath;
            if (UserSession.getInstance().isLoggedIn()) {
                fxmlPath = "/fxml/MainLayout.fxml";
            } else {
                fxmlPath = "/fxml/LoginView.fxml";
            }
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(getClass().getResource(CSS_PATH).toExternalForm());
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("页面加载失败: " + e.getMessage());
        }
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
