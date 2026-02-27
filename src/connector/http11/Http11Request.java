package connector.http11;

import container.session.HttpSession;
import container.session.SessionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Http11Request {

    private String method;
    private String uri;
    private String protocol;
    private String queryString;

    private final Map<String, String> headers = new HashMap<>();
    private final Map<String, String> parameters = new HashMap<>();
    private String body;
    private final Map<String, String> cookies = new HashMap<>();

    private String requestedSessionId;
    private boolean isRequestedSessionIdFromCookie = false;

    public Http11Request(InputStream input) throws IOException {
        parse(input);
    }

    private void parse(InputStream input) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        // Request Line 파싱 (예: GET /index.html?id=123 HTTP/1.1)
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) return;

        String[] parts = requestLine.split(" ");
        if (parts.length >= 3) {
            this.method = parts[0];
            parseUriAndQueryString(parts[1]);
            this.protocol = parts[2];
        }

        // Headers 파싱
        String headerLine;
        int contentLength = 0;
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            int colonIdx = headerLine.indexOf(":");
            if (colonIdx != -1) {
                String name = headerLine.substring(0, colonIdx).trim().toLowerCase();
                String value = headerLine.substring(colonIdx + 1).trim();
                headers.put(name, value);

                // 쿠키는 별도로 파싱하여 저장
                if ("cookie".equals(name)) {
                    parseCookies(value);
                }

                //Body 있는지 확인
                if ("content-length".equals(name)) {
                    contentLength = Integer.parseInt(value);
                }
            }
        }

        // Body/Parameters 파싱 (POST 방식 등)
        if (contentLength > 0) {
            char[] bodyChars = new char[contentLength];
            int read = reader.read(bodyChars, 0, contentLength);
            this.body = new String(bodyChars, 0, read);

            // 만약 application/x-www-form-urlencoded 방식이라면 파라미터로 추출
            String contentType = headers.get("content-type");
            if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                parseParameters(this.body);
            }
        }
    }

    private void parseUriAndQueryString(String uriPart) {
        int questionIdx = uriPart.indexOf("?");
        if (questionIdx != -1) {
            this.uri = uriPart.substring(0, questionIdx);
            this.queryString = uriPart.substring(questionIdx + 1);
            parseParameters(this.queryString);
        } else {
            this.uri = uriPart;
        }
    }

    private void parseParameters(String query) {
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                parameters.put(kv[0], kv[1]);
            }
        }
    }

    private void parseCookies(String cookieHeader) {
        // 형식: JSESSIONID=abc12345; theme=dark
        String[] rawCookies = cookieHeader.split(";");
        for (String rawCookie : rawCookies) {
            String[] kv = rawCookie.split("=");
            if (kv.length == 2) {
                String key = kv[0].trim();
                String value = kv[1].trim();
                cookies.put(key, value);

                if ("JSESSIONID".equalsIgnoreCase(key)) {
                    this.requestedSessionId = value;
                    this.isRequestedSessionIdFromCookie = true;
                }
            }
        }
    }

    // --- Getters ---
    public String getMethod() { return method; }
    public String getUri() { return uri; }
    public String getProtocol() { return protocol; }
    public String getHeader(String name) { return headers.get(name.toLowerCase()); }
    public String getParameter(String name) { return parameters.get(name); }
    public String getCookie(String name) { return cookies.get(name); }
    public String getRequestedSessionId() { return requestedSessionId; }
    public boolean isRequestedSessionIdFromCookie() { return isRequestedSessionIdFromCookie; }
    public String getQueryString() {return queryString; }
    public String getBody() { return body; }

    @Override
    public String toString() {
        return String.format("Http11Request [method=%s, uri=%s, parameters=%s, sessionId=%s]",
                method, uri, parameters, requestedSessionId);
    }
}