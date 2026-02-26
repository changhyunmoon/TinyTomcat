package connector.protocol;

import java.io.IOException;

public interface HttpResponse {
    void setStatus(int statusCode);
    void setHeader(String name, String value);
    void addHeader(String name, String value);
    void setContentType(String type);
    void setBody(byte[] body);
    void setBody(String body);

    // 응답 전송 및 상태 관리
    void send() throws IOException;
    void finish() throws IOException;
    boolean isCommitted(); // 헤더가 이미 전송되었는지 확인
}