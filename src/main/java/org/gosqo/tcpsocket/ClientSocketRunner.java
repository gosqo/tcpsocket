package org.gosqo.tcpsocket;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ClientSocketRunner implements Runnable {
    private static final Logger log = Logger.getLogger("ClientSocketRunner");

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final Consumer<String> chatMessageHandler;
    private final Consumer<String> appMessageHandler;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    String host;
    int port;
    private Thread transmitThread;
    private Thread receiveThread;
    private Socket socket;
    private int communicateIndex;

    public ClientSocketRunner(
            Consumer<String> chatMessageHandler
            , Consumer<String> appMessageHandler
    ) {
        this.chatMessageHandler = chatMessageHandler;
        this.appMessageHandler = appMessageHandler;
    }

    @Override
    public void run() {
        try {
            if (this.socket != null && !socket.isClosed()) {
                appMessageHandler.accept("Socket is running on " + socket.getLocalPort());

                return;
            }

            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 500);

            appMessageHandler.accept("\nSocket connected to: " + socket.getRemoteSocketAddress());

            Socket finalSocket = socket;
            communicateIndex = 0;

            transmitThread = new Thread(() -> handleTransmit(finalSocket), "=Client Transmitter");
            receiveThread = new Thread(() -> handleReceive(finalSocket), "=Client Receiver");

            transmitThread.start();
            receiveThread.start();
        } catch (Exception e) {
            e.printStackTrace(new PrintStream(System.out));

            String appMessage;
            socket = null;

            if (e instanceof UnknownHostException) {
                appMessage = "unknown host " + e.getMessage();
            } else if (e instanceof SocketTimeoutException) {
                appMessage = e.getMessage() + ".\n\tServer seems not running. "
                        + "If it's running, Please check address and port";
            } else {
                appMessage = e.getMessage();
            }

            appMessageHandler.accept("Error while construct Socket: " + appMessage);
        }
    }

    public String close() {
        String message;

        if (socket == null || socket.isClosed()) {
            return "there's no activated socket. use it after 'Joining to chat'";
        }

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

            message = "\nDisconnected. Client socket closed.";
        } catch (IOException e) {
            e.printStackTrace(new PrintStream(System.out));
            message = e.getMessage();
        }

        return message;
    }

    public boolean addMessageToQueue(String message) {
        if (this.socket != null && !socket.isClosed()) {
            return messageQueue.offer(message);
        }

        return false;
    }

    private void handleTransmit(Socket socket) {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                String message = messageQueue.poll(500, TimeUnit.MILLISECONDS); // 대기 시간을 추가
                if (message != null) {
                    writer.println(message); // 서버로 메시지 전송
                    chatMessageHandler.accept("%04d %s:%d (Me) [%s]: %s".formatted(
                                    communicateIndex++
                                    , socket.getLocalAddress().toString()
                                    , socket.getLocalPort()
                                    , LocalDateTime.now().format(dateTimeFormatter)
                                    , message
                            )
                    );
                }
            }
        } catch (IOException | InterruptedException e) {
            log.warning(e.getMessage());
        }
    }

    private void handleReceive(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                            , StandardCharsets.ISO_8859_1
                    )
            );

            while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                String finalReceived = reader.readLine();
                if (finalReceived == null) break; // null이면 스트림 종료
                chatMessageHandler.accept("%04d %s (Remote) [%s]: %s".formatted(
                                communicateIndex++
                                , socket.getRemoteSocketAddress().toString()
                                , LocalDateTime.now().format(dateTimeFormatter)
                                , finalReceived
                        )
                );
            }
            socket.close();
            appMessageHandler.accept("%n[%s] Socket closed.".formatted(
                    LocalDateTime.now().format(dateTimeFormatter)
            ));
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
