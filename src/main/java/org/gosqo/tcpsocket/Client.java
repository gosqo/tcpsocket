package org.gosqo.tcpsocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Client {

    private static final String EXIT_COMMAND = "/exit";
    private static final Logger log = Logger.getLogger("Client");
    public static int port = 8080;
    public static String host = "127.0.0.1";
    public static AtomicBoolean isOpen = new AtomicBoolean(true); // 클라이언트 종료 플래그

    public static void main(String[] args) {
        try (Socket socket = new Socket(host, port)) {
            System.out.println("서버에 연결되었습니다!");

            Thread inputThread = new Thread(() -> handleInput(socket));
            Thread responseThread = new Thread(() -> handleResponse(socket));

            inputThread.start();
            responseThread.start();

            inputThread.join();
            responseThread.join();
        } catch (Exception e) {
            log.warning("클라이언트 에러: " + e.getMessage());
        }
        System.out.println("클라이언트 종료");
    }

    private static void handleInput(Socket socket) {
        try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {

            while (isOpen.get()) {
                String inputValue = consoleInput.readLine();

                if (EXIT_COMMAND.equals(inputValue)) {
                    System.out.println("클라이언트 종료 요청");
                    isOpen.set(false);
                    writer.println(Server.QUIT_CLIENT_COMMAND); // 서버에 클라이언트 종료 요청 전송
                    break;
                }

                writer.println(inputValue); // 서버로 메시지 전송
            }
        } catch (IOException e) {
            System.out.println("입력 처리 에러: " + e.getMessage());
        }
    }

    private static void handleResponse(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            while (isOpen.get()) {
                String serverResponse = reader.readLine();

                if (serverResponse == null) {
                    System.out.println("서버와 연결이 종료되었습니다.");
                    isOpen.set(false);
                    break;
                }

                System.out.println("서버 응답: " + serverResponse);
            }
        } catch (IOException e) {
            System.out.println("서버 응답 처리 에러: " + e.getMessage());
        }
    }
}
