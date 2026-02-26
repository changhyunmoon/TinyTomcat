package connector.protocol;

import java.util.Map;

public interface HttpRequest {
    String getMethod();      // GET, POST 등
    String getUri();         // 전체 URI (ex: /hello?name=gemini)
    String getRequestURI();  // [추가] 순수 경로 (ex: /hello)
    String getProtocol();    // HTTP/1.1 등

    int getStreamId();
    String getHeader(String name);
    Map<String, String> getHeaders();

    String getParameter(String name);
    Map<String, String[]> getParameterMap();

    byte[] getBody();
    boolean isKeepAlive();
}