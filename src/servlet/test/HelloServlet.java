package servlet.test;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;
import servlet.HttpServlet;

public class HelloServlet extends HttpServlet {

    @Override
    public void doGet(HttpRequest request, HttpResponse response) throws Exception {
        String name = request.getParameter("name");
        if (name == null) name = "TinyTomcat";

        String body = "<h1>Hello, " + name + "!</h1>" +
                "<p>Welcome to your custom Servlet Container.</p>";

        // HttpResponse에 결과를 담아 전송 (구현하신 response 구조에 맞춰 호출)
        response.setStatus(200);
        response.setHeader("Content-Type", "text/html; charset=utf-8");
        response.setBody(body.getBytes());
    }
}