package connector.protocol.http11;

import connector.protocol.AbstractRequest;
import connector.protocol.HttpRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
    HTTP 1.1 Request Protocol

    Request Line : 서버가 이 요청을 어떻게 처리할지 결정하는 핵심 정보를 담고 있다
        형식 : [Method] [Request-URI] [HTTP-Version]
        예시: GET /index.html HTTP/1.1

    Request Headers : 요청에 대한 추가 정보를 Key: Value 형태롤 담는다
        Host: 요청이 전송되는 호스트의 도메인명 (HTTP/1.1에서 필수 헤더).
        User-Agent: 클라이언트 소프트웨어(브라우저 등)의 정보.
        Accept: 클라이언트가 처리 가능한 미디어 타입 (text/html, application/json 등).
        Content-Length: 본문(Body)의 바이트 길이 (Body가 있을 때 필수).

    Emty Line : 빈 줄. 헤더의 끝을 알린다.

    Message Body : 실제 전송할 데이터가 담기는 부분
        Get 에는 보통 바디가 없다
        Post, Put 요청 시 사용자가 입력한 폼 데이터나 JSON 데이터 등이 담긴다

    예시
        POST /api/user HTTP/1.1\r\n          <-- Request Line
        Host: localhost:8080\r\n             <-- Header 시작
        Content-Type: application/json\r\n
        Content-Length: 26\r\n
        User-Agent: Insomnia/2023.5.8\r\n
        \r\n                                 <-- Empty Line (매우 중요!)
        {"name": "Gemini", "age": 28}        <-- Message Body
 */

public class Http11Request extends AbstractRequest {

    public Http11Request(InputStream input) throws IOException {
        // 1. Request Line 읽기 (예: POST /hello?id=123 HTTP/1.1)
        String requestLine = readLine(input);
        if (requestLine == null || requestLine.isEmpty()) return;
        parseRequestLine(requestLine);

        // 2. Request Headers 읽기 (빈 줄이 나올 때까지)
        String headerLine;
        while (!(headerLine = readLine(input)).isEmpty()) {
            parseHeader(headerLine);
        }

        // 3. Message Body 읽기 (Content-Length 기준)
        parseBody(input);
    }

    private void parseRequestLine(String line) {
        String[] parts = line.split(" ");
        if (parts.length < 3) return;

        this.method = parts[0];
        String fullUri = parts[1];
        this.protocol = parts[2];

        // URI와 QueryString 분리 (? 기준)
        int questionMarkIndex = fullUri.indexOf('?');
        if (questionMarkIndex != -1) {
            this.uri = fullUri.substring(0, questionMarkIndex);
            // AbstractRequest에 구현된 파라미터 파싱 로직 호출
            super.parseParameters(fullUri.substring(questionMarkIndex + 1));
        } else {
            this.uri = fullUri;
        }
    }

    private void parseHeader(String line) {
        int colonIndex = line.indexOf(':');
        if (colonIndex != -1) {
            String key = line.substring(0, colonIndex).trim();
            String value = line.substring(colonIndex + 1).trim();
            this.headers.put(key, value);
        }
    }

    private void parseBody(InputStream input) throws IOException {
        String contentLengthStr = headers.get("Content-Length");
        if (contentLengthStr != null) {
            int length = Integer.parseInt(contentLengthStr);
            this.body = new byte[length];

            int totalRead = 0;
            while (totalRead < length) {
                int read = input.read(this.body, totalRead, length - totalRead);
                if (read == -1) break;
                totalRead += read;
            }
        }
    }

    /**
     * InputStream에서 CRLF(\r\n)를 만날 때까지 한 줄을 읽습니다.
     */
    private String readLine(InputStream input) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = input.read()) != -1) {
            if (b == '\r') {
                int next = input.read();
                if (next == '\n') break; // \r\n 발견 시 종료
                baos.write(b);
                baos.write(next);
            } else {
                baos.write(b);
            }
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    @Override
    public int getStreamId() {
        return 0; // HTTP/1.1은 단일 스트림이므로 0 반환
    }

    @Override
    public boolean isKeepAlive() {
        String connection = headers.get("Connection");
        if ("close".equalsIgnoreCase(connection)) return false;
        return "keep-alive".equalsIgnoreCase(connection) || "HTTP/1.1".equals(protocol);
    }
}