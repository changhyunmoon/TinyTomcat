package connector.http11;

import connector.HttpHandler;

import java.io.*;
import java.net.Socket;

public class Http11Processor {

    private final HttpHandler handler;

    public Http11Processor(HttpHandler handler) {
        this.handler = handler;
    }

    public void process(Socket socket) {
        try (InputStream rawInput = socket.getInputStream();
             OutputStream rawOutput = socket.getOutputStream();
             BufferedInputStream input = new BufferedInputStream(rawInput);
             BufferedOutputStream output = new BufferedOutputStream(rawOutput)) {

            Http11Request request = new Http11Request(input);
            Http11Response response = new Http11Response(output);

            System.out.println("[HTTP1.1 Processor] Processing: " + request.getMethod() + " " + request.getUri());

            if (handler != null) {
                handler.handle(request, response);
            }

            response.finish();
            output.flush();

        } catch (Exception e) {
            System.err.println("[Processor] Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeSocket(socket);
        }
    }

    private void closeSocket(Socket socket) {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}