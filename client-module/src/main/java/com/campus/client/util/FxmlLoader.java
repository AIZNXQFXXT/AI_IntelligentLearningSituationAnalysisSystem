package com.campus.client.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;

public class FxmlLoader {

    private static final String BASE_PATH = "/fxml/";

    public static Parent load(String fxmlName) throws IOException {
        URL resource = FxmlLoader.class.getResource(BASE_PATH + fxmlName);
        if (resource == null) {
            throw new IOException("FXML file not found: " + BASE_PATH + fxmlName);
        }
        FXMLLoader loader = new FXMLLoader(resource);
        return loader.load();
    }

    public static <T> T loadWithController(String fxmlName, Class<T> controllerType) throws IOException {
        URL resource = FxmlLoader.class.getResource(BASE_PATH + fxmlName);
        if (resource == null) {
            throw new IOException("FXML file not found: " + BASE_PATH + fxmlName);
        }
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();
        T controller = loader.getController();
        return controller;
    }

    public static FXMLLoader getLoader(String fxmlName) {
        URL resource = FxmlLoader.class.getResource(BASE_PATH + fxmlName);
        return new FXMLLoader(resource);
    }
}
