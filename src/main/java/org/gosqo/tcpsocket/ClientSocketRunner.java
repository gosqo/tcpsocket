package org.gosqo.tcpsocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ClientSocketRunner implements Runnable {
    private static final Logger log = Logger.getLogger("ClientSocketRunner");
    private static final int INPUT_BUFFER_SIZE = 512;
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
    private boolean showHex;
    private boolean enterHex;

    public ClientSocketRunner(
            Consumer<String> chatMessageHandler
            , Consumer<String> appMessageHandler
    ) {
        this.chatMessageHandler = chatMessageHandler;
        this.appMessageHandler = appMessageHandler;
    }

    public void setHost(String host) {
        this.host = host;
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

    @Override
    public void run() {
        try {
            if (this.socket != null && !socket.isClosed()) {
                appMessageHandler.accept("Socket is running on " + socket.getLocalPort());

                return;
            }

            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 1500);

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

            if (transmitThread != null && transmitThread.isAlive()) {
                transmitThread.interrupt();
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
        try (OutputStream out = socket.getOutputStream()) {

            while (true) {
                final String entered = messageQueue.poll(500, TimeUnit.MILLISECONDS); // 대기 시간을 추가

                if (entered != null) {
                    String addCrLf = UiStateReflector.addCrLf(enterHex,
                            entered); // temp code: feat add CR, LF would be coded.
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
                                    , socket.getLocalAddress().toString()
                                    , socket.getLocalPort()
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

    private void handleReceive(Socket socket) {
        try (InputStream in = socket.getInputStream()) {
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

                chatMessageHandler.accept("%04d %s (Remote) [%s]: %s".formatted(
                                communicateIndex++
                                , socket.getRemoteSocketAddress().toString()
                                , LocalDateTime.now().format(dateTimeFormatter)
                                , toShow
                        )
                );
            }
            this.close();
            appMessageHandler.accept("%n[%s] Socket closed.".formatted(
                    LocalDateTime.now().format(dateTimeFormatter)
            ));
        } catch (IOException e) {
            log.info(e.getMessage());

            this.close();
            appMessageHandler.accept("%n[%s] Exception from Server: %s".formatted(
                    LocalDateTime.now().format(dateTimeFormatter)
                    , e.getMessage()
            ));
        }
    }
}
