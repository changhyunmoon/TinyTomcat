package connector.protocol;

import connector.protocol.http11.Http11Processor;
import connector.protocol.http20.Http20Processor;
import container.TinyContext;

import java.io.PushbackInputStream;
import java.net.Socket;

public class HttpProtocolHandler {

    private final TinyContext context;
    // HTTP/2.0 연결 시 시작되는 고유 문자열 (Magic String / Connection Preface)
    private static final byte[] H2_MAGIC = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes();

    public HttpProtocolHandler(TinyContext context) {
        this.context = context;
    }

    public void handle(Socket socket) {
        try {
            // PushbackInputStream: 읽은 데이터를 다시 스트림으로 'unread' 할 수 있는 도구
            // H2_MAGIC 길이인 24바이트만큼 버퍼를 잡습니다.
            PushbackInputStream pbis = new PushbackInputStream(socket.getInputStream(), 24);

            byte[] buffer = new byte[24];
            int readCount = pbis.read(buffer);

            if (readCount <= 0) {
                socket.close();
                return;
            }

            // 1. 읽은 바이트를 다시 스트림으로 돌려보내서 Processor가 처음부터 읽게 함
            pbis.unread(buffer, 0, readCount);

            // 2. HTTP/2.0 여부 판단
            if (isHttp2(buffer)) {
                System.out.println("[TinyTomcat] Protocol Detected: HTTP/2.0");
                new Http20Processor().process(pbis, socket.getOutputStream(), context);
            } else {
                System.out.println("[TinyTomcat] Protocol Detected: HTTP/1.1");
                new Http11Processor().process(pbis, socket.getOutputStream(), context);
            }

        } catch (Exception e) {
            System.err.println("[TinyTomcat] Error handling protocol: " + e.getMessage());
        } finally {
            // 소켓 종료 처리는 Processor 내부의 Keep-Alive 로직에 따라 결정되도록 위임하거나
            // 여기서 일괄 처리할 수 있습니다.
        }
    }

    private boolean isHttp2(byte[] buffer) {
        // 버퍼의 앞부분이 H2 Magic String과 일치하는지 확인
        if (buffer.length < H2_MAGIC.length) return false;
        for (int i = 0; i < H2_MAGIC.length; i++) {
            if (buffer[i] != H2_MAGIC[i]) return false;
        }
        return true;
    }
}