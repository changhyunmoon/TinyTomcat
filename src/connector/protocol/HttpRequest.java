package connector.protocol;

import java.util.Map;

public interface HttpRequest {
    String getMethod();
    String getUri();
    String getProtocol();
    int getStreamId(); // HTTP/1.1은 0, HTTP/2는 양수
    String getHeader(String name);
    Map<String, String> getHeaders();
    String getParameter(String name);
    Map<String, String[]> getParameterMap();
    byte[] getBody();
    boolean isKeepAlive();
}