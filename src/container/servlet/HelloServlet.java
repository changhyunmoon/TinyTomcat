package container.servlet;

import connector.http11.dto.Http11Request;
import connector.http11.dto.Http11Response;
import container.session.HttpSession; // 세션 객체 사용

public class HelloServlet extends BaseServlet {

    @Override
    public void init() {
        System.out.println("[HelloServlet] 서블릿이 생성되었습니다. (init 호출)");
    }

    @Override
    protected void doGet(Http11Request request, Http11Response response) throws Exception {
        // 1. Adapter가 request에 심어준 세션 꺼내기
        HttpSession session = request.getSession();
        int count = 1;

        if (session != null) {
            // 세션에서 방문 횟수(visitCount)를 가져옴
            Integer currentCount = (Integer) session.getAttribute("visitCount");
            if (currentCount != null) {
                count = currentCount + 1;
            }
            // 업데이트된 횟수를 다시 세션에 저장
            session.setAttribute("visitCount", count);
        }

        // 2. 응답 작성
        response.setStatus(200, "OK");
        response.setHeader("Content-Type", "text/html; charset=utf-8");

        response.println("<html><body style='font-family: sans-serif; line-height: 1.6; padding: 20px;'>");
        response.println("  <h1>👋 안녕하세요! Tiny Tomcat 서블릿입니다.</h1>");
        response.println("  <div style='background: #f0f0f0; padding: 15px; border-radius: 8px;'>");
        response.println("    <p><b>요청 URI:</b> " + request.getUri() + "</p>");
        response.println("    <p><b>당신의 세션 ID:</b> <code style='color: blue;'>" + (session != null ? session.getId() : "없음") + "</code></p>");
        response.println("    <p><b>방문 횟수:</b> <span style='font-size: 1.2em; color: red;'>" + count + "</span>번</p>");
        response.println("  </div>");
        response.println("  <p>이 페이지는 서버에서 실시간으로 생성되었습니다.</p>");
        response.println("  <a href='/hello' style='display: inline-block; margin-top: 10px; padding: 10px 20px; background: #007bff; color: white; text-decoration: none; border-radius: 5px;'>다시 새로고침 하기</a>");
        response.println("</body></html>");
    }

    @Override
    public void destroy() {
        System.out.println("[HelloServlet] 서블릿이 소멸됩니다. (destroy 호출)");
    }
}