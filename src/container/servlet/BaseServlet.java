package container.servlet;

import connector.http11.dto.Http11Request;
import connector.http11.dto.Http11Response;
import container.TinyServlet;

// 추상 클래스로 선언하여 직접 객체를 만들지는 못하게 합니다.
public abstract class BaseServlet implements TinyServlet {

    @Override
    public void init() {
        // 모든 서블릿이 공통으로 사용할 초기화 로직 (필요하면 오버라이드)
    }

    @Override
    public void service(Http11Request request, Http11Response response) throws Exception {
        // 1. 여기서 HTTP 메서드(GET, POST 등)를 분석합니다.
        String method = request.getMethod();

        // 2. 분석 결과에 따라 적절한 메서드로 배달(Dispatch)합니다.
        if ("GET".equalsIgnoreCase(method)) {
            doGet(request, response);
        } else if ("POST".equalsIgnoreCase(method)) {
            doPost(request, response);
        } else {
            // 지원하지 않는 메서드인 경우 에러 처리 등을 할 수 있습니다.
        }
    }

    // 하위 서블릿들이 필요에 따라 골라 쓸 수 있도록 기본 메서드를 만들어둡니다.
    protected void doGet(Http11Request request, Http11Response response) throws Exception {
        // 구현하지 않고 GET 요청이 오면 "지원하지 않음" 응답을 보낼 수도 있습니다.
    }

    protected void doPost(Http11Request request, Http11Response response) throws Exception {
    }

    @Override
    public void destroy() {
    }
}