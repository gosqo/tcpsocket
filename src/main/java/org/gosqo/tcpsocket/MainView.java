package org.gosqo.tcpsocket;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainView {

    boolean isServerMode = false;

    Label modeSelectionLabel;
    RadioButton serverModeButton, clientModeButton;
    TextArea chatConsole, appMessageConsole;
    TextField ipAddressInput, portInput, chatInput;
    Button serverStartButton, clientStartButton, chatSendButton;
    ToggleGroup modeToggleGroup;

    VBox connectForm;

    void initElements() {
        // mode selection
        modeSelectionLabel = new Label("Select a mode as");
        modeToggleGroup = new ToggleGroup();
        clientModeButton = new RadioButton("Client");
        clientModeButton.setToggleGroup(modeToggleGroup);
        clientModeButton.setSelected(true);
        serverModeButton = new RadioButton("Server");
        serverModeButton.setToggleGroup(modeToggleGroup);
        modeToggleGroup.selectedToggleProperty().addListener(
                (observableValue, toggle, t1) -> {
                    isServerMode = t1 == serverModeButton;
                    toggleConnectFormComponent();
                }
        );

        // connect form
        connectForm = new VBox(10);
        ipAddressInput = new TextField();
        ipAddressInput.setPromptText("Enter server IP Address.");
        portInput = new TextField();
        portInput.setPromptText("Enter the server port.");
        serverStartButton = new Button("Start Server");
        clientStartButton = new Button("Join Server");

        connectForm.getChildren().setAll(clientModeConnectForm());

        // consoles
        appMessageConsole = new TextArea("app message appears here.");
        appMessageConsole.setEditable(false);
        chatConsole = new TextArea();
        chatConsole.setEditable(false);
        chatInput = new TextField();
        chatInput.setPromptText("chat here.");
        chatSendButton = new Button("Send");
    }

    private void toggleConnectFormComponent() {
        connectForm.getChildren().setAll(
                isServerMode ? serverModeConnectForm() : clientModeConnectForm()
        );
    }

    VBox consoleComponent() {
        VBox component = new VBox(10);
        component.getChildren().addAll(
                appMessageConsole
                , chatConsole
                , chatInput
                , chatSendButton
        );

        return component;
    }

    VBox serverModeConnectForm() {
        VBox component = new VBox(10);
        component.getChildren().addAll(
                portInput
                , serverStartButton
        );

        return component;
    }

    VBox clientModeConnectForm() {
        VBox component = new VBox(10);
        component.getChildren().addAll(
                ipAddressInput
                , portInput
                , clientStartButton
        );

        return component;
    }

    VBox modeSelectionComponent() {
        HBox radioButtons = new HBox(10);
        radioButtons.getChildren().addAll(
                clientModeButton
                , serverModeButton
        );

        VBox component = new VBox(10);
        component.getChildren().addAll(
                modeSelectionLabel
                , radioButtons
        );

        return component;
    }

    Scene createScene() {
        initElements();

        VBox layout = new VBox(10);

        layout.setPadding(new Insets(20.0));
        layout.getChildren().addAll(
                modeSelectionComponent()
                , connectForm
                , consoleComponent()
        );

        return new Scene(layout, 400, 600);
    }
}
