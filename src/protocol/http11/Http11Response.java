package protocol.http11;

import protocol.HttpResponse;
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
    private final Map<String, String> headers = new LinkedHashMap<>(); // 순서 유지를 위해 LinkedHashMap
    private byte[] body = new byte[0];
    private static final Map<Integer, String> statusMessages = new HashMap<>();

    static {
        statusMessages.put(200, "OK");
        statusMessages.put(404, "Not Found");
        statusMessages.put(500, "Internal Server Error");
    }

    public Http11Response(OutputStream output) {
        this.output = output;
        // 기본 헤더 설정
        addHeader("Server", "TinyTomcat/1.0");
        addHeader("Content-Type", "text/html; charset=utf-8");
    }

    @Override
    public void setStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    @Override
    public void setBody(byte[] body) {
        this.body = (body != null) ? body : new byte[0];
    }

    @Override
    public void setBody(String body) {
        if (body != null) {
            this.body = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    @Override
    public void send() throws IOException {
        //헤더 정보를 바이트로 변환하기 위해 StringBuilder 활용
        StringBuilder headerBuilder = new StringBuilder();

        // Status Line
        String statusMessage = statusMessages.getOrDefault(statusCode, "Unknown");
        headerBuilder.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusMessage).append("\r\n");

        // Content-Length 갱신
        headers.put("Content-Length", String.valueOf(body.length));

        // Headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            headerBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        // Empty Line
        headerBuilder.append("\r\n");

        // 2. 헤더 전송 (UTF-8 바이트로 변환)
        output.write(headerBuilder.toString().getBytes(StandardCharsets.UTF_8));

        // 3. 바디 전송
        if (body.length > 0) {
            output.write(body);
        }

        // 4. 즉시 전송 보장
        output.flush();

        // 주의: 여기서 output.close()를 하지 않아야 Keep-Alive 구현이 가능합니다.
    }
}

