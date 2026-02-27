package container.servlet;

import connector.http11.dto.Http11Request;
import connector.http11.dto.Http11Response;

public class NotFoundServlet extends BaseServlet {
    @Override
    protected void doGet(Http11Request request, Http11Response response) throws Exception {
        response.setStatus(404, "Not Found");
        response.setHeader("Content-Type", "text/html; charset=utf-8");

        response.println("<html><body style='text-align: center; font-family: sans-serif; padding-top: 100px;'>");
        response.println("  <h1 style='font-size: 80px; color: #ccc;'>404</h1>");
        response.println("  <h2>길을 잃으셨나요?</h2>");
        response.println("  <p>요청하신 경로 <b>" + request.getUri() + "</b>는 존재하지 않는 페이지입니다.</p>");
        response.println("  <br><a href='/hello' style='color: #007bff; text-decoration: none;'>메인으로 돌아가기</a>");
        response.println("</body></html>");
    }
}