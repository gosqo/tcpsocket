package org.gosqo.tcpsocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientSocketRunner implements Runnable {
    private static final Logger log = Logger.getLogger("ClientSocketRunner");

    String host;
    int port;

    private static void handleInput(Socket socket) {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            String inputValue;
            while ((inputValue = consoleInput.readLine()) != null) {
                writer.println(inputValue); // 서버로 메시지 전송
            }
        } catch (IOException e) {
            log.warning("클라이언트 입력 처리 에러: " + e.getMessage());
        }
    }

    private void handleResponse(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String serverResponse;

            while ((serverResponse = reader.readLine()) != null) {
                System.out.println("서버 메시지: " + serverResponse);
            }
        } catch (IOException e) {
            System.out.println("서버 응답 처리 에러: " + e.getMessage());
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket(host, port);
            log.info("서버에 연결되었습니다!");

            Socket finalSocket = socket;
            Thread inputThread = new Thread(() -> handleInput(finalSocket), "client's input handler");
            Thread responseThread = new Thread(() -> handleResponse(finalSocket), "client's response handler");

            inputThread.start();
            responseThread.start();

            inputThread.join(); // 필요시
            responseThread.join(); // 필요시
        } catch (Exception e) {
            log.warning("클라이언트 에러: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    log.warning("소켓 닫기 실패: " + e.getMessage());
                }
            }
        }
    }
}
