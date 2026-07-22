package com.aicampus.util;

import com.aicampus.controller.WebViewContainerController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import com.aicampus.App;

import java.io.IOException;
import java.net.URL;

public class ViewLoader {

    public static final String VUE_APP_PATH = "/vue-app/index.html";

    public static void loadFXMLInto(BorderPane borderPane, String fxmlPath) {
        try {
            URL resource = ViewLoader.class.getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("FXML not found: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Node node = loader.load();
            borderPane.setCenter(node);
        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() ->
                CrudHelper.showError("页面加载失败: " + fxmlPath + "\n" + e.getMessage()));
        }
    }

    public static void loadWebView(BorderPane borderPane, String route) {
        try {
            URL resource = ViewLoader.class.getResource("/fxml/WebViewContainer.fxml");
            if (resource == null) {
                throw new IOException("FXML not found: /fxml/WebViewContainer.fxml");
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Node node = loader.load();
            WebViewContainerController controller = loader.getController();
            borderPane.setCenter(node);
            controller.loadRoute(route);
        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() ->
                CrudHelper.showError("页面加载失败: " + route + "\n" + e.getMessage()));
        }
    }

    public static void loadScene(String fxmlPath, String title) {
        try {
            URL resource = ViewLoader.class.getResource(fxmlPath);
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Stage stage = App.getPrimaryStage();
            Scene scene = new Scene(root, 1200, 800);
            scene.getStylesheets().add(App.getCssPath());
            stage.setScene(scene);
            if (title != null) {
                stage.setTitle(title);
            }
        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() ->
                CrudHelper.showError("页面加载失败: " + fxmlPath + "\n" + e.getMessage()));
        }
    }

    public static <T> T loadFXMLWithController(String fxmlPath) {
        try {
            URL resource = ViewLoader.class.getResource(fxmlPath);
            FXMLLoader loader = new FXMLLoader(resource);
            loader.load();
            return loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
