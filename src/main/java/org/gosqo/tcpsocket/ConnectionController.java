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
            Consumer<String> chatMessageHandler
            , Consumer<String> appMessageHandler

    ) {
        this.client = new ClientSocketRunner(
                chatMessageHandler
                , appMessageHandler
        );
        this.server = new ServerSocketRunner(
                chatMessageHandler
                , appMessageHandler
        );
    }

    // server
    Response startServer(String port, boolean showHex, boolean enterHex, boolean addCr, boolean addLf) {
        // validation
        if (port == null || port.isBlank()) {
            return new Response(400
                    , "port cannot be blank."
                    + "\n\tplease enter numbers set port.");
        }

        try {
            int portNumber = Integer.parseInt(port);

            server.setPort(portNumber);
        } catch (NumberFormatException e) {
            return new Response(400
                    , "port should be value of number."
                    + "\n\tplease enter numbers set port.");
        }

        // service
        try {
            server.run(); // new runnable thread

            server.setShowHex(showHex);
            server.setEnterHex(enterHex);
            server.setAddCr(addCr);
            server.setAddLf(addLf);
        } catch (Exception e) {
            return new Response(400
                    , "port " + server.getPort() + " is " + e.getMessage());
        }

        return new Response(200
                , "\nServer is ready, waiting for client.");
    }

    Response stopServer() {
        boolean isClosed = server.close();

        if (!isClosed) {
            return new Response(500
                    , "exception occurred on closing server.\n"
                    + "please try again later.");
        }

        return new Response(200
                , "\nServer has been stopped.");
    }

    void makeServerListen(Consumer<String> appMessageHandler) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            appMessageHandler.accept(server.listenTilEstablished());

            executor.shutdown();
        });
    }

    // client
    void runClient(String ipAddress, String port, boolean showHex, boolean enterHex, boolean addCr, boolean addLf) {
        client.setHost(ipAddress);
        client.setPort(Integer.parseInt(port));
        client.setShowHex(showHex);
        client.setEnterHex(enterHex);
        client.setAddCr(addCr);
        client.setAddLf(addLf);

        client.run();
    }

    void disconnect(Consumer<String> appMessageHandler) {
        appMessageHandler.accept(client.close());
    }

    // in common(client, server)
    void showHex(boolean isServer) {
        if (isServer) {
            server.setShowHex(true);
            return;
        }

        client.setShowHex(true);
    }

    void disableShowHex(boolean isServer) {
        if (isServer) {
            server.setShowHex(false);
            return;
        }

        client.setShowHex(false);
    }

    void enterHex(boolean isServer) {
        if (isServer) {
            server.setEnterHex(true);
            return;
        }

        client.setEnterHex(true);
    }

    void disableEnterHex(boolean isServer) {
        if (isServer) {
            server.setEnterHex(false);
            return;
        }

        client.setEnterHex(false);
    }

    void addCr(boolean isServer) {
        if (isServer) {
            server.setAddCr(true);
            return;
        }

        client.setAddCr(true);
    }

    void disableAddCr(boolean isServer) {
        if (isServer) {
            server.setAddCr(false);
            return;
        }

        client.setAddCr(false);
    }

    void addLf(boolean isServer) {
        if (isServer) {
            server.setAddLf(true);
            return;
        }

        client.setAddLf(true);
    }

    void disableAddLf(boolean isServer) {
        if (isServer) {
            server.setAddLf(false);
            return;
        }

        client.setAddLf(false);
    }

    Response sendMessage(String message, boolean isServer) {
        boolean serverSent, clientSent;
        try {
            if (isServer) {
                serverSent = server.addMessageToQueue(message);

                if (!serverSent) {
                    return new Response(500
                            , "Cannot add message to Queue, " +
                            "client socket may not constructed or closed.");
                }

                return new Response(200
                        , "message added to Queue");
            }

            clientSent = client.addMessageToQueue(message);

            if (!clientSent) {
                return new Response(500
                        , "Cannot add message to Queue, " +
                        "client socket may not constructed or closed.");
            }

            return new Response(200, "message added to Queue");
        } catch (RuntimeException e) {
            return new Response(500
                    , e.getMessage()); // while adding message to messageQueue
        }
    }

    void stopOnCloseStage(boolean isServer) {
        if (isServer) {
            server.close();
        } else {
            client.close();
        }
    }
}
