package apps.blog;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;
import container.servlet.HttpServlet;

public class ListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws Exception {
        // 1. 응답 데이터(HTML) 생성
        String content = "<html>" +
                "<body>" +
                "  <h1>Tiny Tomcat Blog List</h1>" +
                "  <ul>" +
                "    <li>1. How to implement Connector</li>" +
                "    <li>2. How to implement Container</li>" +
                "  </ul>" +
                "</body>" +
                "</html>";

        // 2. 응답 설정
        response.setStatus(200);
        response.setHeader("Content-Type", "text/html; charset=utf-8");

        // 3. 바디 데이터 전달 (문자열을 바이트 배열로 변환)
        response.setBody(content.getBytes("UTF-8"));
    }
}