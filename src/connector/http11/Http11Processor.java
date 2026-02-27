package connector.http11;

import connector.HttpHandler;
import connector.http11.dto.Http11Request;
import connector.http11.dto.Http11Response;

import java.io.*;
import java.net.Socket;

public class Http11Processor {

    private final HttpHandler handler;

    public Http11Processor(HttpHandler handler) {
        this.handler = handler;
    }

    public void process(Socket socket) {
        // [중요] try-with-resources에서 socket과 stream을 제거합니다.
        // 여기서 스트림을 닫으면 연결된 소켓까지 같이 닫히기 때문입니다.
        try {
            InputStream rawInput = socket.getInputStream();
            OutputStream rawOutput = socket.getOutputStream();

            // 보조 스트림도 close() 시 소켓을 닫으므로 직접 관리합니다.
            BufferedInputStream input = new BufferedInputStream(rawInput);
            BufferedOutputStream output = new BufferedOutputStream(rawOutput);

            Http11Request request = new Http11Request(input);
            Http11Response response = new Http11Response(output);

            System.out.println("[HTTP1.1 Processor] Processing: " + request.getMethod() + " " + request.getUri());

            if (handler != null) {
                handler.handle(request, response);
            }

            // 응답 완료 및 버퍼 비우기
            response.finish();
            output.flush();

        } catch (Exception e) {
            System.err.println("[Processor] Error: " + e.getMessage());
            e.printStackTrace();
            // 에러가 발생했을 때만 예외적으로 소켓을 닫아 리소스를 해제합니다.
            closeSocket(socket);
        }
        // [수정] finally 블록에서 무조건 소켓을 닫던 로직을 삭제했습니다.
    }

    private void closeSocket(Socket socket) {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}