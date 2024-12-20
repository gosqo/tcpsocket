package org.gosqo.tcpsocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ServerSocketRunner implements Runnable {
    private static final Logger log = Logger.getLogger("ServerSocketRunner");
    private static final int INPUT_BUFFER_SIZE = 512;
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
    private boolean addCr;
    private boolean addLf;

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

    public void setShowHex(boolean showHex) {
        this.showHex = showHex;
    }

    public void setEnterHex(boolean enterHex) {
        this.enterHex = enterHex;
    }

    public void setAddCr(boolean addCr) {
        this.addCr = addCr;
    }

    public void setAddLf(boolean addLf) {
        this.addLf = addLf;
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
        try (OutputStream out = clientSocket.getOutputStream()) {

            while (true) {
                final String entered = messageQueue.poll(500, TimeUnit.MILLISECONDS); // 대기 시간을 추가

                if (entered != null) {
                    String addCrLf = UiStateReflector.addCrLf(addCr, addLf, enterHex, entered);
                    final byte[] bytes;
                    int length;

                    try {
                        bytes = UiStateReflector.getBytes(enterHex, addCrLf);
                        length = bytes.length;
                    } catch (IllegalArgumentException e) {
                        appMessageHandler.accept("Cannot parse to byte " + e.getMessage()
                                + ".\n\tPlease check whether 'Enter in Hex' mode is on."
                                + "\n\tOtherwise enter between (hexadecimal) 00 - ff for each byte."
                        );
                        continue;
                    }

                    out.write(bytes, 0, length);
                    out.flush();

                    final String toShow = UiStateReflector.decideHowToShow(showHex, bytes, length);

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

            if (e instanceof InterruptedException) {
                log.warning(Thread.currentThread().getName() + " interrupted and closed.");
                return;
            }

            log.warning(e.getMessage());
        }
    }

    private void handleReceive(Socket clientSocket) {
        try (InputStream in = clientSocket.getInputStream()) {
            byte[] buffer = new byte[INPUT_BUFFER_SIZE];

            while (true) { // reader.readLine(): 대기 지점. 클라이언트 측에서 접속 해제 시 = null . while 문 탈출
                int length = in.read(buffer);

                if (length == -1) {
                    break;
                }
                String toShow = UiStateReflector.decideHowToShow(showHex, buffer, length);

                if (communicateIndex == 0) {
                    chatMessageHandler.accept("");
                }

                chatMessageHandler.accept("%04d %s (Client) [%s]: %s".formatted(
                                communicateIndex++
                                , clientSocket.getRemoteSocketAddress().toString()
                                , LocalDateTime.now().format(dateTimeFormatter)
                                , toShow
                        )
                );
            }

            this.close();

            appMessageHandler.accept("%n[%s] client %s socket closed.".formatted(
                            LocalDateTime.now().format(dateTimeFormatter)
                            , clientSocket.getRemoteSocketAddress().toString()
                    )
            );
        } catch (IOException e) { // 서버 측에서 close() 호출 시 this.clientSocket.close() 하며 예외 발생. 로깅
            log.warning(e.getMessage());

            this.close();
            appMessageHandler.accept("%n[%s] Exception from client %s %s".formatted(
                            LocalDateTime.now().format(dateTimeFormatter)
                            , clientSocket.getRemoteSocketAddress().toString()
                            , e.getMessage()
                    )
            );
        }
    }
}
