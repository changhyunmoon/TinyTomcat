package container;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;
import container.servlet.Servlet;

public class Wrapper implements Container {
    private final Servlet servlet;

    public Wrapper(Servlet servlet) {
        this.servlet = servlet;
        this.servlet.init(); // 래퍼 생성 시점에 서블릿 초기화
    }

    @Override
    public void invoke(HttpRequest request, HttpResponse response) throws Exception {
        // 서블릿의 service 메서드 호출
        servlet.service(request, response);
    }
}