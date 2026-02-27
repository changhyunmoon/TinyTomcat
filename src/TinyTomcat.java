
import connector.ConnectorAdapter;
import connector.HttpConnector;
import connector.http11.Http11Mapper;
import container.TinyContext;
import container.TinyWrapper;
import container.servlet.HelloServlet;
import container.session.SessionManager;

public class TinyTomcat {

    public static void main(String[] args) {
        int port = 8080;

        // 공통 인프라 생성 (세션 관리자)
        SessionManager sessionManager = new SessionManager();

        // 컨테이너 생성 (Context)
        // 실제 톰캣의 'Web Application' 하나에 해당합니다.
        TinyContext context = new TinyContext(sessionManager);

        // 서블릿 및 매퍼 설정
        Http11Mapper mapper = new Http11Mapper();
        mapper.setContext(context);

        // --- [사용자 서블릿 등록 구역] ---

        // HelloServlet 등록 (/hello)
        HelloServlet helloServlet = new HelloServlet();
        TinyWrapper helloWrapper = new TinyWrapper("HelloServlet", helloServlet);
        context.addChild("HelloServlet", helloWrapper); // 컨텍스트에 보관
        mapper.addWrapper("/hello", helloWrapper);      // 매퍼에 길 등록

//        // TimeServlet 등록 (/time)
//        TimeServlet timeServlet = new TimeServlet();
//        TinyWrapper timeWrapper = new TinyWrapper("TimeServlet", timeServlet);
//        context.addChild("TimeServlet", timeWrapper);   // 컨텍스트에 보관
//        mapper.addWrapper("/time", timeWrapper);        // 매퍼에 길 등록

        // ------------------------------

        // 어댑터 연결 (Connector와 Container의 다리)
        // Mapper와 SessionManager를 주입받아 요청을 배달할 준비를 합니다.
        ConnectorAdapter adapter = new ConnectorAdapter(sessionManager, mapper);

        // 커넥터 생성
        HttpConnector connector = new HttpConnector(port, adapter);

        // 엔진 가동
        System.out.println("========================================");
        System.out.println("🚀 Tiny Tomcat Engine Starting...");
        System.out.println("📍 Port: " + port);
        System.out.println("🔗 Hello: http://localhost:8080/hello");
        System.out.println("🔗 Time:  http://localhost:8080/time");
        System.out.println("========================================");

        // 커넥터를 별도 스레드에서 실행
        Thread serverThread = new Thread(connector);
        serverThread.start();
    }
}