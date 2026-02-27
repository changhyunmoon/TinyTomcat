package container.servlet;

import connector.http11.dto.Http11Request;
import connector.http11.dto.Http11Response;

public class ErrorServlet extends BaseServlet {

    @Override
    public void service(Http11Request request, Http11Response response) throws Exception {
        response.setStatus(500, "Internal Server Error");
        response.setHeader("Content-Type", "text/html; charset=utf-8");

        String errorMessage = (String) request.getAttribute("error_message");

        response.println("<html><body style='background-color: #fff5f5; padding: 50px; font-family: monospace;'>");
        response.println("  <h1 style='color: #e53e3e;'>🚨 500 Internal Server Error</h1>");
        response.println("  <hr>");
        response.println("  <h3>서버 내부에서 문제가 발생했습니다.</h3>");
        if (errorMessage != null) {
            response.println("  <div style='background: #eee; padding: 15px; border-radius: 5px;'>" + errorMessage + "</div>");
        }
        response.println("  <p>잠시 후 다시 시도해 주세요.</p>");
        response.println("</body></html>");
    }

}