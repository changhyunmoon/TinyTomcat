package connector;

import connector.http11.Http11Processor;
import connector.HttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

public class HttpConnector implements Runnable {

    private final int port;
    private final HttpHandler handler;
    private final ExecutorService workerPool;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private volatile boolean stopped = false;

    public HttpConnector(int port, HttpHandler handler) {
        this.port = port;
        this.handler = handler;

        // --- 스레드 풀 설정 ---
        int corePoolSize = 10;     // 최소 유지 스레드
        int maxPoolSize = 100;    // 최대 확장 스레드
        long keepAliveTime = 60L; // 유휴 스레드 생존 시간
        int queueCapacity = 50;   // 대기 큐 크기

        // 커스텀 ThreadPoolExecutor 생성
        this.workerPool = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity), // 큐 크기를 제한하여 OOM 방지
                new ThreadPoolExecutor.CallerRunsPolicy() // 큐가 꽉 차면 요청한 스레드에서 직접 처리 (Back-pressure)
        );
    }

    @Override
    public void run() {
        try {
            // Selector와 ServerSocketChannel 오픈
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();

            // 비차단(Non-blocking) 모드 설정 및 바인딩
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(port));

            // ServerSocketChannel을 Selector에 등록 (Accept 이벤트 감시)
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("[NIO Connector] Server started on port " + port);

            while (!stopped) {
                // 이벤트가 발생할 때까지 대기 (Blocking 가능하지만, 스레드 하나만 소모)
                if (selector.select(1000) == 0) continue;

                // 발생한 이벤트(SelectionKey)들을 가져옴
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove(); // 처리 시작 전 반드시 제거

                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        // 새로운 연결 요청이 들어온 경우
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        // 기존 연결에서 데이터가 들어온 경우
                        // 핵심: 스레드를 점유하지 않도록 관심을 해제하고 Worker에게 위임
                        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                        workerPool.execute(() -> handleRead(key));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false); // 클라이언트 소켓도 비차단 모드

        // 클라이언트 채널을 Selector에 등록 (Read 이벤트 감시)
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("[NIO Connector] Accepted connection from: " + client.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        try {
            // 1. [수정] key.cancel()을 명시적으로 부르지 말고,
            // 일단 블로킹 모드로 바꿉니다. (NIO 채널은 등록된 상태에서도 가능은 하지만 예외가 날 수 있음)
            // 만약 에러가 계속된다면, 아예 새로 등록하는 방식을 취합니다.
            if (key.isValid()) {
                key.cancel(); // 여기서 취소하면 Selector의 다음 select() 때 정리됩니다.
            }

            client.configureBlocking(true);

            Http11Processor processor = new Http11Processor(handler);
            processor.process(client.socket());

        } catch (Exception e) {
            System.err.println("[Connector] Processor Error: " + e.getMessage());
            closeSocket(client);
        } finally {
            try {
                if (client.isOpen()) {
                    client.configureBlocking(false);
                    // 2. [핵심] 핵심은 wakeup()과 register()의 순서입니다.
                    // 기존 키가 cancel되었으므로 새로 register를 해줘야 합니다.
                    selector.wakeup();
                    client.register(selector, SelectionKey.OP_READ);
                }
            } catch (Exception e) {
                // 재등록 과정에서 에러가 나면 소켓을 닫는 것이 안전합니다.
                closeSocket(client);
            }
        }
    }

    public void stop() {
        this.stopped = true;
        try {
            if (selector != null) selector.close();
            if (serverChannel != null) serverChannel.close();
            workerPool.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 안전한 소켓 닫기를 위한 헬퍼 메서드 추가
    private void closeSocket(SocketChannel client) {
        try {
            if (client != null) client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}