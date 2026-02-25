package protocol.http11;

import protocol.HttpRequest;
import protocol.HttpResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Http11Processor {

    public void process(Socket socket) {
        System.out.println("[Debug] New Request from: " + socket.getRemoteSocketAddress());

        try {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            HttpRequest request = new Http11Request(input);

            if (request.getUri() == null) {
                return;
            }

            HttpResponse response = new Http11Response(output);

            if ("/".equals(request.getUri())) {
                response.setStatus(200);
                response.setBody("<h1>Welcome to Tiny Tomcat!</h1>");
            } else {
                response.setStatus(404);
                response.setBody("<h1>404 Not Found</h1>");
            }

            response.send();

        } catch (Exception e) {
            // 소켓이 이미 닫혔을 때 발생하는 예외 등을 캐치
            System.err.println("[Error] Processor 처리 중 예외 발생: " + e.getMessage());
        }
    }
}