package protocol.http11;

import connector.ProtocolHandler;

import java.io.IOException;
import java.net.Socket;

public class Http11ProtocolHandler implements ProtocolHandler {

    @Override
    public void handle(Socket socket) {
        try {
            Http11Processor processor = new Http11Processor();
            processor.process(socket);
        } catch (Exception e) {
            System.err.println("Protocol Handling Error: " + e.getMessage());
        } finally {
            closeSocket(socket);
        }
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