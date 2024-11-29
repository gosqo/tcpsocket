package org.gosqo.tcpsocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ServerSocketRunner implements Runnable {
    private static final Logger log = Logger.getLogger("ServerSocketRunner");
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final Consumer<String> receivedMessageHandler;
    private final Consumer<String> appMessageHandler;
    Socket clientSocket;
    ServerSocket serverSocket;
    private int port;
    private Thread receiveThread;
    private Thread transmitThread;
    public ServerSocketRunner(
            Consumer<String> receivedMessageHandler
            , Consumer<String> appMessageHandler
    ) {
        this.receivedMessageHandler = receivedMessageHandler;
        this.appMessageHandler = appMessageHandler;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("=Server Main Thread");

        try {
            serverSocket = new ServerSocket(port);
            log.info("Server is ready to communicate.");
        } catch (IOException e) {
            log.warning(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        log.info("server is listening ...");
    }

    public void close() {
        try {
            if (serverSocket.isClosed()) {
                throw new RuntimeException("Server's" + port + " port has been closed.");
            }

            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            transmitThread.interrupt();
        } catch (IOException e) {
            log.warning("서버 종료 에러: " + e.getMessage());
        }
    }

    public void listenTilEstablished() {
        try {
            clientSocket = serverSocket.accept();
            log.info("클라이언트가 연결되었습니다: " + clientSocket.getRemoteSocketAddress());

            runCommunicate();
        } catch (IOException e) {
            log.warning("클라이언트 연결 에러: " + e.getMessage());
        }
    }

    public void runCommunicate() {

        receiveThread = new Thread(() -> handleReceive(clientSocket), "=Server Receiver");
        transmitThread = new Thread(() -> handleTransmit(clientSocket), "=Server Transmitter");

        receiveThread.start();
        transmitThread.start();
    }

    public void addMessageToQueue(String message) {
        messageQueue.offer(message);
    }

    private void handleTransmit(Socket clientSocket) {
        try {
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            while (!serverSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                String message = messageQueue.poll(500, TimeUnit.MILLISECONDS);
                if (message != null) {
                    writer.println(message);
                }
            }
        } catch (IOException | InterruptedException e) {
            log.warning("서버 입력 핸들러 에러: " + e.getMessage());
        }
    }

    private void handleReceive(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String received;

            while ((received = reader.readLine()) != null) {
                receivedMessageHandler.accept("Client: " + received);
            }
        } catch (IOException e) {
            appMessageHandler.accept("Client: " + e.getMessage());
        }
    }
}
