package org.gosqo.tcpsocket;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ClientSocketRunner implements Runnable {
    private static final Logger log = Logger.getLogger("ClientSocketRunner");

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final Consumer<String> receivedMessageHandler;
    private final Consumer<String> appMessageHandler;
    String host;
    int port;
    private Thread transmitThread;
    private Thread receiveThread;
    private Socket socket;

    public ClientSocketRunner(
            Consumer<String> receivedMessageHandler
            , Consumer<String> appMessageHandler
    ) {
        this.receivedMessageHandler = receivedMessageHandler;
        this.appMessageHandler = appMessageHandler;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("=Client Main Thread");

        try {
            socket = new Socket(host, port);

            Socket finalSocket = socket;
            transmitThread = new Thread(() -> handleTransmit(finalSocket), "=Client Transmitter");
            receiveThread = new Thread(() -> handleReceive(finalSocket), "=Client Receiver");

            transmitThread.start();
            receiveThread.start();

        } catch (Exception e) {
            log.warning("클라이언트 에러: " + e.getMessage());
            e.printStackTrace(new PrintStream(System.out));
            appMessageHandler.accept("error while construct Socket: " + e.getMessage());

        }
    }

    public void close() {
        try {

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            if (transmitThread != null) {
                transmitThread.interrupt();
            }

            if (receiveThread != null) {
                receiveThread.interrupt();
            }
        } catch (IOException e) {
            log.warning("client socket close Error: " + e.getMessage());
            appMessageHandler.accept("error while closing: " + e.getMessage());
        }
    }

    public void addMessageToQueue(String message) {
        messageQueue.offer(message);
    }

    private void handleTransmit(Socket socket) {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                String message = messageQueue.poll(500, TimeUnit.MILLISECONDS); // 대기 시간을 추가
                if (message != null) {
                    writer.println(message); // 서버로 메시지 전송
                }
            }
        } catch (IOException | InterruptedException e) {
            log.warning("클라이언트 입력 처리 에러: " + e.getMessage());
        }
    }

    private void handleReceive(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                if (reader.ready()) { // 데이터가 준비된 경우만 읽기
                    String finalReceived = reader.readLine();
                    if (finalReceived == null) break; // null이면 스트림 종료
                    receivedMessageHandler.accept("Server: " + finalReceived);
                }
            }
        } catch (IOException e) {
            receivedMessageHandler.accept("Server Error: " + e.getMessage());
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
