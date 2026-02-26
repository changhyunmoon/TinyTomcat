package connector.protocol.http11;

import connector.protocol.HttpResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
    Http1.1 Response Protocol

    Status Line : 상태 줄. 요청의 성공 여부를 알려준다
        구성 : [HTTP 버전] [상태 코드] [상태 메시지]
        예시 : HTTP/1.1 200 OK

    Response Headers : 응답 데이터에 대한 부가 정보를 담고 있다

    Empty Line (CRLF) : 헤더와 본문 구분하는 구분선

    Message Body : 클라이언트가 실제로 요청한 데이터가 들어있는 영역
        HTML 문서, 이미지 파일, JSON 데이터 등 다양한 형식이 올 수 있다.

    예시
        HTTP/1.1 200 OK\r\n                  <-- Status Line
        Date: Wed, 25 Feb 2026 14:40:00 GMT\r\n <-- Headers 시작
        Server: TinyTomcat/1.0\r\n
        Content-Type: text/html; charset=UTF-8\r\n
        Content-Length: 45\r\n
        \r\n                                 <-- Empty Line (구분선)
        <html><body>Hello Tiny Tomcat!</body></html> <-- Message Body
 */

public class Http11Response implements HttpResponse {
    private final OutputStream output;
    private int statusCode = 200;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private byte[] body = new byte[0];
    private boolean committed = false;

    private static final Map<Integer, String> statusMessages = new HashMap<>();
    static {
        statusMessages.put(200, "OK");
        statusMessages.put(404, "Not Found");
        statusMessages.put(500, "Internal Server Error");
    }

    public Http11Response(OutputStream output) {
        this.output = output;
        setHeader("Server", "TinyTomcat/1.0");
        setHeader("Content-Type", "text/html; charset=utf-8");
    }

    @Override public void setStatus(int statusCode) { this.statusCode = statusCode; }
    @Override public void setHeader(String name, String value) { headers.put(name, value); }
    @Override public void addHeader(String name, String value) { headers.put(name, value); }
    @Override public void setContentType(String type) { setHeader("Content-Type", type); }
    @Override public void setBody(byte[] body) { this.body = (body != null) ? body : new byte[0]; }
    @Override public void setBody(String body) {
        if (body != null) this.body = body.getBytes(StandardCharsets.UTF_8);
    }

    @Override public boolean isCommitted() { return committed; }

    @Override
    public void send() throws IOException {
        if (committed) return;

        StringBuilder sb = new StringBuilder();
        // 1. Status Line
        String msg = statusMessages.getOrDefault(statusCode, "Unknown");
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(msg).append("\r\n");

        // 2. Headers (Content-Length 자동 계산)
        headers.put("Content-Length", String.valueOf(body.length));
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        // 3. Empty Line
        sb.append("\r\n");

        // 전송
        output.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        if (body.length > 0) {
            output.write(body);
        }
        output.flush();
        this.committed = true;
    }

    @Override
    public void finish() throws IOException {
        if (!committed) send();
        // HTTP/1.1에서는 여기서 close를 하지 않고 flush만 하여 Keep-Alive를 유지합니다.
        output.flush();
    }
}