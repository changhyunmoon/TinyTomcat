package servlet;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;

/*
    최상위 인터페이스 servlet
 */
public interface Servlet {

    //서블릿 초기화
    void init() throws Exception;

    //실제 로직 처리
    void service(HttpRequest request, HttpResponse response) throws Exception;

    //자원 해제
    void destroy();
}
