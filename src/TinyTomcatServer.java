import connector.HttpConnector;
import connector.ProtocolHandler;
import protocol.http11.Http11ProtocolHandler;

public class TinyTomcatServer {

    public static void main(String[] args) {
        // 사용할 프로토콜 핸들러 선택 (HTTP/1.1)
        ProtocolHandler handler = new Http11ProtocolHandler();

        // 커넥터 생성 (8080 포트)
        HttpConnector connector = new HttpConnector(8080, handler);

        // 서버 시작
        connector.start();

        System.out.println(">>> Tiny Tomcat is running. Access http://localhost:8080 in your browser.");
    }
}