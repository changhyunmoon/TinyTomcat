package protocol;

import java.util.Map;

public interface HttpRequest {
    String getMethod();
    String getUri();
    String getProtocol();
    String getHeader(String name);
    Map<String, String> getHeaders();
    byte[] getBody();
}
