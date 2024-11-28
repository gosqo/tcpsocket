package org.gosqo.tcpsocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class ServerSocketRunner implements Runnable {
    private static final Logger log = Logger.getLogger("ServerSocketRunner");
    Socket clientSocket;
    ServerSocket serverSocket;
    private int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void run() {

        try {
            serverSocket = new ServerSocket(port);
            log.info("Server is ready to communicate.");
        } catch (IOException e) {
            log.warning(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        log.info("server is listening ...");
    }

    public void stopServer() {
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

        Thread clientHandler = new Thread(() -> handleClient(clientSocket), "server's client handler");
        Thread serverInputHandler = new Thread(() -> handleServerInput(clientSocket), "server input handler");

        clientHandler.start();
        serverInputHandler.start();
    }

    private void handleServerInput(Socket clientSocket) {
        try {
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            String input;
            while ((input = consoleInput.readLine()) != null) {
                writer.println(input);
            }
        } catch (IOException e) {
            log.warning("서버 입력 핸들러 에러: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String message;

            while ((message = reader.readLine()) != null) {
                System.out.println("클라이언트 메시지: " + message);
            }
        } catch (IOException e) {
            log.warning("클라이언트 핸들러 에러: " + e.getMessage());
        }
    }
}
