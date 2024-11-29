package org.gosqo.tcpsocket;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.logging.Logger;

public class MainView {

    private static final Logger log = Logger.getLogger("MainView");
    private final ConnectionController connectionController = new ConnectionController();
    boolean isServerMode = false;

    Label modeSelectionLabel;
    RadioButton serverModeButton, clientModeButton;
    TextArea chatConsole, appMessageConsole;
    TextField ipAddressInput, portInput, chatInput;
    Button serverStartButton, clientStartButton, chatSendButton, serverStopButton;
    ToggleGroup modeToggleGroup;

    VBox connectForm;

    private void initElements() {
        // mode selection
        modeSelectionLabel = new Label("Select a mode as");
        modeToggleGroup = new ToggleGroup();
        clientModeButton = new RadioButton("Client");
        serverModeButton = new RadioButton("Server");

        clientModeButton.setToggleGroup(modeToggleGroup);
        clientModeButton.setSelected(true);

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
        portInput = new TextField();
        serverStartButton = new Button("Start Server");
        clientStartButton = new Button("Join to Chat");
        serverStopButton = new Button("Stop Server");

        ipAddressInput.setPromptText("Enter server IP Address.");

        portInput.setPromptText("Enter the server port.");

        connectForm.getChildren().setAll(clientModeConnectForm());

        // consoles
        appMessageConsole = new TextArea();
        chatConsole = new TextArea();
        chatInput = new TextField();
        chatSendButton = new Button("Send");

        appMessageConsole.setPromptText("app message appears here.");
        appMessageConsole.setEditable(false);

        chatConsole.setEditable(false);

        chatInput.setPromptText("chat here.");

        // set element event handlers
        addElementsHandlers();
    }

    // events
    private void toggleConnectFormComponent() {
        connectForm.getChildren().setAll(
                isServerMode ? serverModeConnectForm() : clientModeConnectForm()
        );
    }

    private void addElementsHandlers() {
        serverStartButton.setOnAction(event -> startServer());
        serverStopButton.setOnAction(event -> stopServer());

        clientStartButton.setOnAction(event -> startClient());
    }

    // in common (client, server)
    private void appendAppMessageConsole(String message) {
        Platform.runLater(() -> appMessageConsole.appendText(message + "\n"));
    }

    // client
    private void startClient() {
        String ipAddress = ipAddressInput.getText();
        String port = portInput.getText();

        connectionController.runClient(ipAddress, port);
    }

    // server
    private void startServer() {
        String port = portInput.getText();
        Response startResponse = connectionController.startServer(port);

        if (startResponse.status() == 200) {
            Response listenResponse = connectionController.makeServerListen();
        }

        appendAppMessageConsole(startResponse.message());
    }

    private void stopServer() {
        Response response = connectionController.stopServer();

        if (response.status() == 200) {
            appendAppMessageConsole(response.message());
        }
    }

    // ui components
    private VBox consoleComponent() {
        VBox component = new VBox(10);
        component.getChildren().addAll(
                appMessageConsole
                , chatConsole
                , chatInput
                , chatSendButton
        );

        return component;
    }

    private VBox serverModeConnectForm() {
        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(
                serverStartButton
                , serverStopButton
        );

        VBox component = new VBox(10);
        component.getChildren().addAll(
                portInput
                , buttons
        );

        return component;
    }

    private VBox clientModeConnectForm() {
        VBox component = new VBox(10);
        component.getChildren().addAll(
                ipAddressInput
                , portInput
                , clientStartButton
        );

        return component;
    }

    private VBox modeSelectionComponent() {
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
