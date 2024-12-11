package org.gosqo.tcpsocket;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class ChatApp extends Application {

    private static final MainView mainView = new MainView();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = mainView.createScene();
        URL mainStylesheet = Objects.requireNonNull(
                getClass().getResource("main.css")
        );

        scene.getStylesheets().add(mainStylesheet.toExternalForm());

        stage.setTitle("TCP socket");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> mainView.stageClose());
        stage.show();
    }
}
