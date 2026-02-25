package connector;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpConnector {

    private final int port;
    private final ProtocolHandler protocolHandler;
    private final ExecutorService executorService;
    private boolean running = false;

    public HttpConnector(int port, ProtocolHandler protocolHandler) {
        this.port = port;
        this.protocolHandler = protocolHandler;
        // 동시에 20개의 요청을 처리할 수 있는 쓰레드 풀 생성
        this.executorService = Executors.newFixedThreadPool(20);
    }

    public void start() {
        this.running = true;

        // 별도의 쓰레드에서 서버 루프를 실행하여 메인 쓰레드가 차단되지 않게 함
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("[TinyTomcat] Connector started on port " + port);

                while (running) {
                    try {
                        // 1. 클라이언트 접속 대기 (Acceptor)
                        Socket clientSocket = serverSocket.accept();

                        // 2. 쓰레드 풀을 통해 비동기로 프로토콜 처리 위임
                        executorService.execute(() -> {
                            protocolHandler.handle(clientSocket);
                        });

                    } catch (IOException e) {
                        if (running) {
                            System.err.println("[TinyTomcat] Error accepting connection: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("[TinyTomcat] Could not start server on port " + port);
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() {
        this.running = false;
        executorService.shutdown();
        System.out.println("[TinyTomcat] Connector stopped.");
    }
}