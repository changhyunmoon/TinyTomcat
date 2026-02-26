import connector.HttpConnector;
import connector.protocol.http11.Http11ProtocolHandler;
import container.Context;
import container.Wrapper;
import container.mapping.Mapper;
import apps.blog.ListServlet; // 예시 서블릿

public class TinyTomcatServer {
    // 서버 전체의 지도를 들고 있는 Mapper
    private static final Mapper mapper = new Mapper();

    public static void main(String[] args) {
        // 1. 애플리케이션(Context) 및 서블릿 등록
        // "C:/webapps/blog"는 실제 파일이 저장된 위치(배포 경로)라고 가정합니다.
        Context blogApp = new Context("/blog", "C:/webapps/blog");
        blogApp.addServlet("/list", new Wrapper(new ListServlet()));

        // 2. 매퍼에 앱 등록
        mapper.addContext("/blog", blogApp);

        // 3. 커넥터 설정 및 시작
        Http11ProtocolHandler handler = new Http11ProtocolHandler();
        HttpConnector connector = new HttpConnector(8080, handler);
        connector.start();

        System.out.println(">>> Tiny Tomcat is running with Mapper.");
    }

    // [중요] Connector/Processor가 요청 파싱을 마치고 호출할 진입점
    public static Mapper getMapper() {
        return mapper;
    }
}