package container;

import connector.http11.dto.Http11Request;
import connector.http11.dto.Http11Response;

public class TinyWrapper {

    private final String name;
    private final TinyServlet servlet; // 실제 서블릿 (알맹이)
    private TinyContext parent;        // 내가 소속된 컨텍스트
    private boolean initialized = false;

    public TinyWrapper(String name, TinyServlet servlet) {
        this.name = name;
        this.servlet = servlet;
    }

    public void setParent(TinyContext parent) {
        this.parent = parent;
    }

    public void execute(Http11Request request, Http11Response response) throws Exception {
        // [Lazy Loading] 처음 호출될 때 딱 한 번만 init() 실행
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    servlet.init();
                    initialized = true;
                    System.out.println("[Wrapper] " + name + " Servlet Initialized.");
                }
            }
        }
        // 실제 서블릿의 비즈니스 로직 호출
        servlet.service(request, response);
    }
}