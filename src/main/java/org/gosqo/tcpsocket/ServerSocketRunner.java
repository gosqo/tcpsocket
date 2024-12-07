package org.gosqo.tcpsocket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ServerSocketRunner implements Runnable {
    private static final Logger log = Logger.getLogger("ServerSocketRunner");
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final Consumer<String> chatMessageHandler;
    private final Consumer<String> appMessageHandler;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    Socket clientSocket;
    ServerSocket serverSocket;
    private int port;
    private Thread receiveThread;
    private Thread transmitThread;
    private int communicateIndex;
    private boolean showHex;
    private boolean enterHex;

    public ServerSocketRunner(
            Consumer<String> chatMessageHandler
            , Consumer<String> appMessageHandler
    ) {
        this.chatMessageHandler = chatMessageHandler;
        this.appMessageHandler = appMessageHandler;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isShowHex() {
        return showHex;
    }

    public void setShowHex(boolean showHex) {
        this.showHex = showHex;
    }

    public void setEnterHex(boolean enterHex) {
        this.enterHex = enterHex;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            communicateIndex = 0;

            log.info("Server is ready to communicate.");
        } catch (IOException e) {
            log.warning(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        log.info("server is listening ...");
    }

    public boolean close() {
        try {

            // handleReceive.reader.readLine 대기 상태에서 clientSocket 종료로
            // IOException 발생으로 receiveThread 종료 됨.
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close(); // 클라이언트 측 앱 상에서 클라이언트 송수신 스레드 종료.
            }

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            if (transmitThread != null && transmitThread.isAlive()) {
                transmitThread.interrupt();
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace(new PrintStream(System.out));
            log.warning("Exception while closing server: " + e.getMessage());
        }

        return false;
    }

    public String listenTilEstablished() {
        String message;

        try {
            clientSocket = serverSocket.accept();
            message = "\nConnected to client: " + clientSocket.getRemoteSocketAddress();
            log.info(message);

            runCommunicate();
        } catch (IOException e) {
            message = "Exception while accepting client: " + e.getMessage();
        }

        return message;
    }

    public void runCommunicate() {

        receiveThread = new Thread(() -> handleReceive(clientSocket), "=Server Receiver");
        transmitThread = new Thread(() -> handleTransmit(clientSocket), "=Server Transmitter");

        receiveThread.start();
        transmitThread.start();
    }

    public boolean addMessageToQueue(String message) {
        if (clientSocket != null && !clientSocket.isClosed()) {
            return messageQueue.offer(message);
        }

        return false;
    }

    private void handleTransmit(Socket clientSocket) {
        try {
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            while (!serverSocket.isClosed() && !Thread.currentThread().isInterrupted()) {
                final String entered = messageQueue.poll(500, TimeUnit.MILLISECONDS); // 대기 시간을 추가

                if (entered != null) {

                    try {
                        final String toSend = convertIfEnterHex(entered);

                        writer.println(toSend); // 서버로 메시지 전송
                    } catch (IllegalArgumentException e) {
                        appMessageHandler.accept(e.getMessage());
                        continue;
                    }

                    final String toShow = decideHowToShow(entered);

                    chatMessageHandler.accept("%04d %s:%d (Me) [%s]: %s".formatted(
                                    communicateIndex++
                                    , serverSocket.getInetAddress().getHostAddress()
                                    , serverSocket.getLocalPort()
                                    , LocalDateTime.now().format(dateTimeFormatter)
                                    , toShow
                            )
                    );
                }
            }
        } catch (IOException | InterruptedException e) {
            log.warning(e.getMessage());
        }
    }

    private void handleReceive(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            );

            String received;

            while ((received = reader.readLine()) != null) { // reader.readLine(): 대기 지점. 클라이언트 측에서 접속 해제 시 = null . while 문 탈출
                String toShow = convertIfShowHex(received);

                chatMessageHandler.accept("%04d %s (Client) [%s]: %s".formatted(
                                communicateIndex++
                                , clientSocket.getRemoteSocketAddress().toString()
                                , LocalDateTime.now().format(dateTimeFormatter)
                                , toShow
                        )
                );
            }

            clientSocket.close();
            serverSocket.close();

            appMessageHandler.accept("%n[%s] client %s socket closed.".formatted(
                            LocalDateTime.now().format(dateTimeFormatter)
                            , clientSocket.getRemoteSocketAddress().toString()
                    )
            );
        } catch (IOException e) { // 서버 측에서 close() 호출 시 this.clientSocket.close() 하며 예외 발생. 로깅
            log.warning(e.getMessage());

            try {
                clientSocket.close();
                serverSocket.close();
            } catch (IOException eOnClose) {
                eOnClose.printStackTrace(new PrintStream(System.out));
            }

            transmitThread.interrupt();

            appMessageHandler.accept("%n[%s] Exception from client %s %s".formatted(
                            LocalDateTime.now().format(dateTimeFormatter)
                            , clientSocket.getRemoteSocketAddress().toString()
                            , e.getMessage()
                    )
            );
        }
    }

    private String decideHowToShow(String entered) {

        if (enterHex && showHex) return HexConverter.separateEach2(entered);
        if (enterHex && !showHex) return HexConverter.hexStringToUTF_8Encoded(entered);
        if (!enterHex && showHex) return HexConverter.stringToHex(entered);
        return entered;
    }

    private String convertIfEnterHex(String message) {
        return enterHex ? HexConverter.hexStringToUTF_8Encoded(message) : message;
    }

    private String convertIfShowHex(String message) {
        return showHex ? HexConverter.stringToHex(message) : message;
    }
}
