package org.gosqo.tcpsocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

public class Server {
    static final String QUIT_CLIENT_COMMAND = "/quitClient";
    private static final String QUIT_SERVER_COMMAND = "/quitServer";
    private static final Logger log = Logger.getLogger("Server");

    public static int port = 8080;
    public static boolean isListening = true;  // 서버 소켓 종료 플래그
    public static boolean isEstablished = false;  // 클라이언트 연결 플래그

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("서버가 대기 중...");

        while (isListening) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("클라이언트가 연결되었습니다!");
            isEstablished = true;

            Thread clientHandler = new Thread(() -> handleClient(clientSocket));
            Thread serverInputHandler = new Thread(() -> handleServerInput(clientSocket));

            clientHandler.start();
            serverInputHandler.start();

            try {
                clientHandler.join();
                serverInputHandler.join();
            } catch (InterruptedException e) {
                log.warning("서버 쓰레드가 중단되었습니다.");
                Arrays.stream(e.getStackTrace())
                        .forEach(item -> log.warning(item.toString()));
            }
        }

        System.out.println("서버 종료");
        serverSocket.close();
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            while (isEstablished) {
                String message = reader.readLine();
                if (message == null) continue;

                System.out.println("클라이언트 메시지: " + message);

                if (message.equals(QUIT_CLIENT_COMMAND)) {
                    System.out.println("클라이언트 연결 종료 요청");
                    isEstablished = false;
                    break;
                }

                if (message.equals(QUIT_SERVER_COMMAND)) {
                    System.out.println("서버 종료 요청");
                    isListening = false;
                    isEstablished = false;
                    break;
                }
            }
        } catch (IOException e) {
            log.warning("클라이언트 핸들러 에러: " + e.getMessage());
        }
    }

    private static void handleServerInput(Socket clientSocket) {
        try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            while (isEstablished) {
                String input = consoleInput.readLine();

                if (input.equals(QUIT_CLIENT_COMMAND)) {
                    writer.println(QUIT_CLIENT_COMMAND);
                    isEstablished = false;
                    break;
                }

                if (input.equals(QUIT_SERVER_COMMAND)) {
                    writer.println(QUIT_SERVER_COMMAND);
                    isListening = false;
                    isEstablished = false;
                    break;
                }

                writer.println(input);
            }
        } catch (IOException e) {
            log.warning("서버 입력 핸들러 에러: " + e.getMessage());
        }
    }
}
