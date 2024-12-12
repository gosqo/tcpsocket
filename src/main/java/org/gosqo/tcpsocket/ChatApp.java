package org.gosqo.tcpsocket;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class ChatApp extends Application {

    public static final String APP_VERSION = "1.1.0";
    private static final MainView mainView = new MainView();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        String title = "TCP socket by gosqo " + APP_VERSION;
        Scene scene = mainView.createScene();
        URL mainStylesheet = Objects.requireNonNull(
                getClass().getResource("main.css")
        );

        scene.getStylesheets().add(mainStylesheet.toExternalForm());

        stage.setTitle(title);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> mainView.stageClose());
        stage.show();
    }
}
