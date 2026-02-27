package connector.http11;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Http11Response {

    private final OutputStream output;
    private int statusCode = 200;
    private String statusMessage = "OK";
    private String contentType = "text/html;charset=utf-8";
    private final Map<String, String> headers = new HashMap<>();
    private final List<String> cookies = new ArrayList<>();
    private final StringBuilder bodyBuilder = new StringBuilder();

    public Http11Response(OutputStream output) {
        this.output = output;
    }

    public void setStatus(int code, String message) {
        this.statusCode = code;
        this.statusMessage = message;
    }

    public void setContentType(String type) {
        this.contentType = type;
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void addCookie(String name, String value) {
        // 간단하게 name=value 형태로 저장 (만료시간, Path 등 추가 가능)
        cookies.add(name + "=" + value + "; Path=/; HttpOnly");
    }

    public void println(String content) {
        bodyBuilder.append(content).append("\n");
    }

    public void finish() throws IOException {
        PrintWriter writer = new PrintWriter(output, true);

        // Status Line (예: HTTP/1.1 200 OK)
        writer.print("HTTP/1.1 " + statusCode + " " + statusMessage + "\r\n");

        // Headers
        writer.print("Content-Type: " + contentType + "\r\n");
        // Body 길이를 계산하여 명시 (Keep-Alive 등을 위해 필수)
        writer.print("Content-Length: " + bodyBuilder.toString().getBytes().length + "\r\n");

        for (Map.Entry<String, String> header : headers.entrySet()) {
            writer.print(header.getKey() + ": " + header.getValue() + "\r\n");
        }

        // Cookies
        for (String cookie : cookies) {
            writer.print("Set-Cookie: " + cookie + "\r\n");
        }

        // Header와 Body 사이의 빈 줄
        writer.print("\r\n");

        // Body
        writer.print(bodyBuilder.toString());

        //전송
        writer.flush();
    }
}