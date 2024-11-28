package org.gosqo.tcpsocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ConnectionController {
    private static final Logger log = Logger.getLogger("ConnectionController");
    private final ServerSocketRunner serverRunner = new ServerSocketRunner();
    private final ClientSocketRunner client = new ClientSocketRunner();

    Response startServer(String port) {
        // validation
        if (port == null || port.isBlank()) {
            return new Response(400, "port cannot be blank." +
                    "\n\tplease enter numbers set port.");
        }

        // exception handling
        try {
            int portNumber = Integer.parseInt(port);

            serverRunner.setPort(portNumber);
        } catch (NumberFormatException e) {
            return new Response(400, "port should be value of number." +
                    "\n\tplease enter numbers set port.");
        }

        // service
        int status = 200;
        String message = "Server is ready, waiting for client.";

        try {
            serverRunner.run();
        } catch (Exception e) {
            status = 400;
            message = "port " + serverRunner.getPort() + " is " + e.getMessage();
        }

        return new Response(status, message);
    }

    Response stopServer() {
        String message = "Server has been stopped.";

        try {

            serverRunner.stopServer();
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
        }

        return new Response(200, message);
    }

    Response makeServerListen() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            serverRunner.listenTilEstablished();
        });

        return new Response(200, "message");
    }

    void runClient(String ipAddress, String port) {
        ExecutorService executor = Executors.newSingleThreadExecutor();


        client.setHost(ipAddress);
        client.setPort(Integer.parseInt(port));

        executor.execute(() -> {
            client.run();
        });
    }
}
