package container;

import connector.http11.dto.Http11Request;
import connector.http11.dto.Http11Response;
import container.session.SessionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.Map;

public class TinyContext {

    // 1. 내부 저장소: 서블릿 이름이나 경로를 기반으로 Wrapper들을 보관합니다.
    private final Map<String, TinyWrapper> children = new HashMap<>();
    private final SessionManager sessionManager;

    public TinyContext(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    // 2. 서블릿 추가: 새로운 서블릿을 보관함에 넣는 로직
    public void addChild(String name, TinyWrapper wrapper) {
        // Wrapper에게 부모(Context)가 누구인지 알려줌 (상호 참조)
        wrapper.setParent(this);
        children.put(name, wrapper);
    }

    /**
     * 3. 실행 로직: Adapter가 요청을 보내면 최종적으로 실행함
     * (여기서 Http11Request가 아닌 추상화된 인터페이스를 쓰는 것이 좋습니다.)
     */
    public void execute(Http11Request request, Http11Response response, TinyWrapper wrapper) {
        try {
            if (wrapper != null) {
                // Wrapper를 통해 실제 서블릿의 service() 메서드 호출
                wrapper.execute(request, response);
            } else {
                send404(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            send500(response, e.getMessage());
        }
    }

    private void send404(Http11Response response) {
        response.setStatus(404, "Not Found");
        response.println("<h1>404 Not Found</h1>");
    }

    private void send500(Http11Response response, String error) {
        response.setStatus(500, "Internal Server Error");
        response.println("<h1>500 Error</h1><p>" + error + "</p>");
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}