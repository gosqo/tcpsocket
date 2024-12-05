package org.gosqo.tcpsocket;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.logging.Logger;

public class MainView {

    private static final Logger log = Logger.getLogger("MainView");
    boolean isServerMode = false;
    Label modeSelectionLabel, hexLabel;
    RadioButton serverModeButton, clientModeButton;
    CheckBox showHex, enterHex;
    TextArea chatConsole, appMessageConsole, chatInput;
    private final ConnectionController connectionController = new ConnectionController(
            this::appendChatMessage
            , this::appendAppMessage
    );
    TextField ipAddressInput, portInput;
    Button serverStartButton, clientStartButton, chatSendButton, serverStopButton, clientDisconnectButton;
    ToggleGroup modeToggleGroup;

    VBox connectForm, consoleComponent, modeComponent, connectComponent, hexComponent;
    HBox modeButtons, controlBox, chatFormComponent;

    private void initElements() {
        // mode selection
        modeComponent = new VBox(10);
        modeButtons = new HBox(10);
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

        modeButtons.getChildren().addAll(clientModeButton, serverModeButton);
        modeComponent.getChildren().addAll(modeSelectionLabel, modeButtons);

        // connect form
        connectForm = new VBox(10);
        ipAddressInput = new TextField();
        portInput = new TextField();
        serverStartButton = new Button("Start Server");
        serverStopButton = new Button("Stop Server");
        clientStartButton = new Button("Join to Chat");
        clientDisconnectButton = new Button("Disconnect");

        ipAddressInput.setPromptText("Enter server IP Address.");
        ipAddressInput.setText("192.168.0.");

        portInput.setPromptText("Enter the server port.");
        portInput.setText("8080");

        connectForm.getChildren().setAll(clientModeConnectForm());

        // hex component
        hexComponent = new VBox(10);
        hexLabel = new Label("Hexadecimal");
        showHex = new CheckBox("Show in Hex");
        enterHex = new CheckBox("Enter in Hex");

        hexComponent.getChildren().addAll(hexLabel, showHex, enterHex);

        // connect component ( mode selection + connect form)
        connectComponent = new VBox(10, modeComponent, connectForm);

        // control box (connect component + hex component
        controlBox = new HBox(20, connectComponent, hexComponent);

        // consoles
        consoleComponent = new VBox(10);
        appMessageConsole = new TextArea();
        chatConsole = new TextArea();
        chatInput = new TextArea();
        chatSendButton = new Button("Send");

        appMessageConsole.setPromptText("app message appears here.");
        appMessageConsole.setEditable(false);
        appMessageConsole.setFocusTraversable(false);

        chatConsole.setPromptText("sent/received chat appears here.");
        chatConsole.setEditable(false);
        chatConsole.setFocusTraversable(false);
        VBox.setVgrow(chatConsole, Priority.ALWAYS);

        chatInput.setPromptText("chat here.");
        chatInput.setPrefHeight(64);

        chatSendButton.setMinWidth(48);
        chatFormComponent = new HBox(10, chatInput, chatSendButton);
        HBox.setHgrow(chatInput, Priority.ALWAYS);

        consoleComponent.getChildren().addAll(
                appMessageConsole
                , chatConsole
                , chatFormComponent
        );
        // set element event handlers
        setButtonsOnAction();
        setCheckBoxEvent();
        addKeyEventHandlers();
    }

    // events
    private void toggleConnectFormComponent() {
        connectForm.getChildren().setAll(
                isServerMode ? serverModeConnectForm() : clientModeConnectForm()
        );
    }

    private void addKeyEventHandlers() {
        ipAddressInput.addEventHandler(KeyEvent.KEY_PRESSED, this::enterKeyFireConnectButton);

        portInput.addEventHandler(KeyEvent.KEY_PRESSED, this::enterKeyFireConnectButton);

        serverStartButton.addEventHandler(KeyEvent.KEY_PRESSED, this::enterKeyFireConnectButton);

        serverStopButton.addEventHandler(KeyEvent.KEY_PRESSED, this::enterKeyFireDisconnectButton);

        clientStartButton.addEventHandler(KeyEvent.KEY_PRESSED, this::enterKeyFireConnectButton);

        clientDisconnectButton.addEventHandler(KeyEvent.KEY_PRESSED, this::enterKeyFireDisconnectButton);

        chatInput.addEventFilter(KeyEvent.KEY_PRESSED, this::makeTabFocusNextComponent);
        chatInput.addEventFilter(KeyEvent.KEY_PRESSED, this::enterKeyFireSendButton);
//        chatInput.addEventFilter(KeyEvent.KEY_PRESSED, this::lineFeed);

        chatSendButton.addEventHandler(KeyEvent.KEY_PRESSED, this::enterKeyFireSendButton);
    }

    private void setButtonsOnAction() {
        serverStartButton.setOnAction(event -> startServer());
        serverStopButton.setOnAction(event -> stopServer());

        clientStartButton.setOnAction(event -> startClient());
        clientDisconnectButton.setOnAction(event -> disconnect());

        chatSendButton.setOnAction(event -> sendMessage());
    }

    private void setCheckBoxEvent() {
        showHex.setOnAction(event -> {
            if (showHex.isSelected()) {
                connectionController.showHex(isServerMode);
                return;
            }

            connectionController.disableShowHex(isServerMode);
        });

        enterHex.setOnAction(event -> {
            if (enterHex.isSelected()) {
                connectionController.enterHex(isServerMode);
                return;
            }

            connectionController.disableEnterHex(isServerMode);
        });
    }

    private void enterKeyFireConnectButton(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
            if (isServerMode) {
                serverStartButton.fire();
                return;
            }

            clientStartButton.fire();
        }
    }

    private void enterKeyFireDisconnectButton(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
            if (isServerMode) {
                serverStopButton.fire();
                return;
            }

            clientDisconnectButton.fire();
        }
    }

    private void makeTabFocusNextComponent(KeyEvent event) {
        // TextArea 에서 TAB 이 '\t' 입력을 기본으로 수행.
        // TAB 에 기존 이벤트를 막고, 다음 노드인 chatSendButton 에 포커스.
        if (event.getCode() == KeyCode.TAB && !event.isShiftDown()) {
            chatSendButton.requestFocus();

            event.consume();
        }
    }

    private void enterKeyFireSendButton(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
            chatSendButton.fire();

            event.consume();
        }
    }

    private void lineFeed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && event.isShiftDown()) {
            int caretPosition = chatInput.getCaretPosition();
            String text = chatInput.getText();
            String lineFed = text.substring(0, caretPosition) + "\n" + text.substring(caretPosition);

            chatInput.setText(lineFed);
            chatInput.positionCaret(caretPosition + 1);

            event.consume();
        }
    }

    void stageClose() {
        connectionController.stopOnCloseStage(isServerMode);
    }

    // in common (client, server)
    private void appendAppMessage(String message) {
        Platform.runLater(() -> appMessageConsole.appendText(message + "\n"));
    }

    private void sendMessage() {
        String message = chatInput.getText();

        Response response = connectionController.sendMessage(message, isServerMode);

        if (response.status() != 200) {
            appendAppMessage(response.message());
        }

//        chatInput.clear(); // uncomment if needed.
    }

    public void appendChatMessage(String message) {
        Platform.runLater(() -> chatConsole.appendText(message + "\n"));
    }

    // client
    private void startClient() {
        String ipAddress = ipAddressInput.getText();
        String port = portInput.getText();

        connectionController.runClient(ipAddress, port);
    }

    private void disconnect() {
        connectionController.disconnect(this::appendAppMessage);
    }

    // server
    private void startServer() {
        String port = portInput.getText();
        Response startResponse = connectionController.startServer(port);

        appendAppMessage(startResponse.message());

        if (startResponse.status() == 200) {
            connectionController.makeServerListen(this::appendAppMessage);
        }
    }

    private void stopServer() {
        Response response = connectionController.stopServer();

        appendAppMessage(response.message());
    }

    // ui components
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
        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(
                clientStartButton
                , clientDisconnectButton
        );

        VBox component = new VBox(10);
        component.getChildren().addAll(
                ipAddressInput
                , portInput
                , buttons
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

        VBox layout = new VBox(16);
        VBox.setVgrow(consoleComponent, Priority.ALWAYS);

        layout.setPadding(new Insets(20.0));
        layout.getChildren().addAll(
                controlBox
                , consoleComponent
        );

        return new Scene(layout, 600, 800);
    }
}
