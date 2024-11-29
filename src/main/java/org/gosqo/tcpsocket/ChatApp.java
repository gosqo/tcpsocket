package org.gosqo.tcpsocket;

import javafx.application.Application;
import javafx.stage.Stage;

public class ChatApp extends Application {

    private static final MainView mainView = new MainView();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(mainView.createScene());
        stage.setOnCloseRequest(event -> mainView.stageClose());
        stage.show();
    }
}
