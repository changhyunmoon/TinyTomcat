package servlet.test;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;
import servlet.HttpServlet;

public class UserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws Exception {
        String id = request.getParameter("id");
        String mode = request.getParameter("mode");

        StringBuilder sb = new StringBuilder();
        sb.append("<h1>User Profile</h1>");
        sb.append("<ul>");
        sb.append("<li>User ID: ").append(id != null ? id : "Unknown").append("</li>");
        sb.append("<li>View Mode: ").append(mode != null ? mode : "Default").append("</li>");
        sb.append("</ul>");
        sb.append("<p>Requested Protocol: ").append(request.getProtocol()).append("</p>");

        response.setStatus(200);
        response.setContentType("text/html; charset=utf-8");
        response.setBody(sb.toString());
    }
}