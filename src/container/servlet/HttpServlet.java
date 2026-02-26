package container.servlet;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;

public abstract class HttpServlet implements Servlet {

    @Override
    public void init() {
        // 기본값은 빈 구현 (필요 시 오버라이드)
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        String method = request.getMethod();

        // HTTP 메서드에 따라 적절한 핸들러로 분기
        if ("GET".equalsIgnoreCase(method)) {
            doGet(request, response);
        } else if ("POST".equalsIgnoreCase(method)) {
            doPost(request, response);
        } else {
            response.setStatus(405); // Method Not Allowed
        }
    }

    protected void doGet(HttpRequest request, HttpResponse response) throws Exception {
        // 하위 클래스에서 오버라이드하지 않으면 405 응답
        response.setStatus(405);
    }

    protected void doPost(HttpRequest request, HttpResponse response) throws Exception {
        response.setStatus(405);
    }

    @Override
    public void destroy() {
        // 기본값은 빈 구현
    }
}