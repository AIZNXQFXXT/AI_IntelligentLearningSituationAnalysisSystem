package com.aicampus.controller;

import com.aicampus.session.UserSession;
import com.aicampus.util.ViewLoader;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

public class WebViewContainerController {

    @FXML private Button btnBack;
    @FXML private Button btnForward;
    @FXML private Button btnRefresh;
    @FXML private Label loadingLabel;
    @FXML private BorderPane webContainer;

    private WebView webView;
    private WebEngine webEngine;

    @FXML
    public void initialize() {
        webView = new WebView();
        webView.setZoom(1.0);
        webEngine = webView.getEngine();

        webContainer.setCenter(webView);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.RUNNING) {
                loadingLabel.setVisible(true);
                loadingLabel.setManaged(true);
            } else if (newState == Worker.State.SUCCEEDED) {
                loadingLabel.setVisible(false);
                loadingLabel.setManaged(false);
                updateNavButtons();
                injectSession();
            } else if (newState == Worker.State.FAILED) {
                loadingLabel.setVisible(false);
                loadingLabel.setManaged(false);
            }
        });
    }

    public void loadRoute(String route) {
        String url = ViewLoader.class.getResource(ViewLoader.VUE_APP_PATH).toExternalForm();
        if (route != null && !route.isEmpty()) {
            url += "#" + route;
        }
        webEngine.load(url);
    }

    @FXML
    private void goBack() {
        WebHistory history = webEngine.getHistory();
        if (history.getCurrentIndex() > 0) {
            history.go(-1);
        }
    }

    @FXML
    private void goForward() {
        WebHistory history = webEngine.getHistory();
        if (history.getCurrentIndex() < history.getEntries().size() - 1) {
            history.go(1);
        }
    }

    @FXML
    private void refresh() {
        webEngine.reload();
    }

    private void updateNavButtons() {
        WebHistory history = webEngine.getHistory();
        btnBack.setDisable(history.getCurrentIndex() <= 0);
        btnForward.setDisable(history.getCurrentIndex() >= history.getEntries().size() - 1);
    }

    private void injectSession() {
        UserSession session = UserSession.getInstance();
        if (session.isLoggedIn()) {
            webEngine.executeScript(
                "localStorage.setItem('campus_token', '" + escapeJs(session.getToken()) + "')"
            );
            webEngine.executeScript(
                "localStorage.setItem('campus_user', '" + escapeJs(session.toJson()) + "')"
            );
        }
    }

    private static String escapeJs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n");
    }
}
