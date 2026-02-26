package connector;

import connector.protocol.HttpProtocolHandler;
import container.TinyContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpConnector {

    private final int port;
    private final HttpProtocolHandler protocolHandler;
    private final ExecutorService workerPool;
    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public HttpConnector(int port, TinyContext context) {
        this.port = port;
        this.protocolHandler = new HttpProtocolHandler(context);

        // 커스텀 ThreadFactory 구현
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            private final String namePrefix = "TinyTomcat-Worker-";

            @Override
            public Thread newThread(Runnable r) {
                // 스레드 생성 시 이름을 부여 (예: TinyTomcat-Worker-1)
                Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
                // 필요 시 데몬 스레드로 설정 가능 (서버 종료 시 함께 종료되도록)
                if (t.isDaemon()) t.setDaemon(false);
                // 우선순위 설정
                if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        };

        // ThreadFactory를 인자로 받는 스레드 풀 생성
        this.workerPool = Executors.newFixedThreadPool(20, threadFactory);
    }

    public void start() {
        this.running = true;

        // Acceptor 스레드: 별도 스레드에서 클라이언트의 접속만 기다림 (Main 스레드 차단 방지)
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("[TinyTomcat] Connector started on port " + port);

                while (running) {
                    try {
                        // 1. 클라이언트 접속 대기 (Blocking)
                        Socket socket = serverSocket.accept();
                        System.out.println("[TinyTomcat] New connection accepted: " + socket.getInetAddress());

                        // 2. 접속된 소켓을 Worker Pool에 던짐 (비동기 처리)
                        workerPool.execute(() -> protocolHandler.handle(socket));

                    } catch (IOException e) {
                        if (running) {
                            System.err.println("[TinyTomcat] Accept error: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("[TinyTomcat] Could not open server socket on port " + port);
                e.printStackTrace();
            }
        }, "Connector-Acceptor").start();
    }

    public void stop() {
        this.running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            // Worker 스레드 풀 안전 종료
            workerPool.shutdown();
            if (!workerPool.awaitTermination(5, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }

            System.out.println("[TinyTomcat] Connector stopped safely.");
        } catch (Exception e) {
            System.err.println("[TinyTomcat] Error during shutdown: " + e.getMessage());
        }
    }
}