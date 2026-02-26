package connector.protocol;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class AbstractRequest implements HttpRequest {
    protected String method;
    protected String uri;
    protected String protocol;
    protected Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    protected Map<String, String[]> parameters = new HashMap<>();
    protected byte[] body;

    protected void parseParameters(String queryString) {
        if (queryString == null || queryString.isEmpty()) return;

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            try {
                // URL 디코딩 적용 (한글이나 공백 처리 필수)
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";

                // 동일한 키로 여러 값이 들어오는 경우 대응 (배열 확장)
                if (parameters.containsKey(key)) {
                    String[] existing = parameters.get(key);
                    String[] updated = Arrays.copyOf(existing, existing.length + 1);
                    updated[existing.length] = value;
                    parameters.put(key, updated);
                } else {
                    parameters.put(key, new String[]{value});
                }
            } catch (Exception e) {
                // 잘못된 인코딩 무시
            }
        }
    }

    @Override public String getMethod() { return method; }
    @Override public String getUri() { return uri; }
    @Override public String getProtocol() { return protocol; }
    @Override public String getHeader(String name) { return headers.get(name); }
    @Override public Map<String, String> getHeaders() { return Collections.unmodifiableMap(headers); }
    @Override public String getParameter(String name) {
        String[] values = parameters.get(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }
    @Override public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }
    @Override public byte[] getBody() { return body; }

    @Override
    public boolean isKeepAlive() {
        String connection = getHeader("Connection");

        // 1. 명시적으로 close가 오면 연결 종료
        if ("close".equalsIgnoreCase(connection)) return false;

        // 2. HTTP/1.1은 기본적으로 Keep-Alive가 활성화됨
        if ("HTTP/1.1".equalsIgnoreCase(protocol)) {
            return !"close".equalsIgnoreCase(connection);
        }

        // 3. HTTP/1.0은 명시적으로 keep-alive가 있어야 함
        return "keep-alive".equalsIgnoreCase(connection);
    }
}