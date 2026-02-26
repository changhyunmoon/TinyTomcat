package servlet.test;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;
import servlet.HttpServlet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws Exception {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String body = "<html><body>" +
                "<h2>Current Server Time</h2>" +
                "<p style='color: blue; font-weight: bold;'>" + now + "</p>" +
                "<button onclick='location.reload()'>Refresh</button>" +
                "</body></html>";

        response.setStatus(200);
        response.setBody(body);
    }
}