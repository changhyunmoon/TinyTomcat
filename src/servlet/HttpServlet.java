package servlet;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;

public abstract class HttpServlet implements Servlet {

    @Override
    public void init() throws Exception {
        // 기본 구현 (필요 시 오버라이드)
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method)) {
            doGet(request, response);
        } else if ("POST".equalsIgnoreCase(method)) {
            doPost(request, response);
        } else {
            // 405 Method Not Allowed 로직이 들어갈 자리
            System.out.println("지원하지 않는 메서드: " + method);
        }
    }

    protected void doGet(HttpRequest request, HttpResponse response) throws Exception {
        // 하위 클래스에서 구현
    }

    protected void doPost(HttpRequest request, HttpResponse response) throws Exception {
        // 하위 클래스에서 구현
    }

    @Override
    public void destroy() {
        // 기본 구현 (필요 시 오버라이드)
    }
}