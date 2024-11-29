package org.gosqo.tcpsocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ConnectionController {
    private static final Logger log = Logger.getLogger("ConnectionController");
    private final ServerSocketRunner server;
    private final ClientSocketRunner client;

    public ConnectionController(
            Consumer<String> receivedMessageHandler
            , Consumer<String> appMessageHandler

    ) {
        this.client = new ClientSocketRunner(
                receivedMessageHandler
                , appMessageHandler
        );

        this.server = new ServerSocketRunner(
                receivedMessageHandler
                , appMessageHandler
        );
    }

    // server
    Response startServer(String port) {
        // validation
        if (port == null || port.isBlank()) {
            return new Response(400, "port cannot be blank." +
                    "\n\tplease enter numbers set port.");
        }

        // exception handling
        try {
            int portNumber = Integer.parseInt(port);

            server.setPort(portNumber);
        } catch (NumberFormatException e) {
            return new Response(400, "port should be value of number." +
                    "\n\tplease enter numbers set port.");
        }

        // service
        int status = 200;
        String message = "Server is ready, waiting for client.";

        try {
            server.run();
        } catch (Exception e) {
            status = 400;
            message = "port " + server.getPort() + " is " + e.getMessage();
        }

        return new Response(status, message);
    }

    Response stopServer() {
        String message = "Server has been stopped.";

        try {

            server.close();
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
        }

        return new Response(200, message);
    }

    Response makeServerListen() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            server.listenTilEstablished();
        });

        return new Response(200, "message");
    }

    // client
    void runClient(String ipAddress, String port) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        client.setHost(ipAddress);
        client.setPort(Integer.parseInt(port));

        client.run();
    }

    // in common(client, server)
    Response sendMessage(String message, boolean isServer) {
        int status = 200;
        String responseMessage = "message transmitted.";

        try {
            if (isServer) {
                server.addMessageToQueue(message);
            } else {
                client.addMessageToQueue(message);
            }
        } catch (RuntimeException e) {
            status = 500;
            responseMessage = "something went wrong.";
        }

        return new Response(200, responseMessage);
    }

    void stopOnCloseStage(boolean isServer) {
        if (isServer) {
            server.close();
        } else {
            client.close();
        }
    }
}
