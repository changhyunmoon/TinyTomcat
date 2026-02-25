package protocol;

import java.io.IOException;

public interface HttpResponse {
    void setStatus(int statusCode);
    void addHeader(String name, String value);
    void setBody(byte[] body);
    void setBody(String body);
    void send() throws IOException;
}