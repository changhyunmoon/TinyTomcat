
import connector.HttpConnector;
import connector.protocol.http11.Http11Request;
import connector.protocol.http11.Http11Response;

import java.net.ServerSocket;
import java.net.Socket;

public class TinyTomcatServer {

    public static void main(String[] args) throws Exception{
        TinyContext context = new TinyContext();
        context.addServlet("/hello", "servlet.test.HelloServlet");

        HttpConnector connector = new HttpConnector(8080, context);
        connector.start();

        // 프로그램 종료 시 stop() 호출을 위한 훅 (선택 사항)
        Runtime.getRuntime().addShutdownHook(new Thread(connector::stop));

    }
}