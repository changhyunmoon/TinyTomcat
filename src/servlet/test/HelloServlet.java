package servlet.test;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;
import servlet.HttpServlet;


public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws Exception {
        // 클라이언트로부터 전달받은 파라미터가 있다면 활용 (HttpRequest에 getParameter가 구현되어 있다고 가정)
        // 현재는 간단하게 고정 메시지를 출력하겠습니다.

        String content = "<html><body>" +
                "<h1>Hello from Tiny Tomcat!</h1>" +
                "<p>Method: " + request.getMethod() + "</p>" +
                "<p>URI: " + request.getUri() + "</p>" +
                "<p>Time: " + new java.util.Date() + "</p>" +
                "</body></html>";

        response.setStatus(200);
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        response.setBody(content);
    }

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) throws Exception {
        // POST 요청은 보통 Body에 데이터가 실려 옵니다.
        String requestBody = new String(request.getBody(), java.nio.charset.StandardCharsets.UTF_8);

        String content = "<html><body>" +
                "<h1>POST Request Received!</h1>" +
                "<p>Your Data: " + requestBody + "</p>" +
                "</body></html>";

        response.setStatus(200);
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        response.setBody(content);
    }

}