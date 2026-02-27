package connector;

import connector.http11.Http11Mapper;
import connector.http11.dto.Http11Request;
import connector.http11.dto.Http11Response;
import connector.http11.dto.MappingData;
import container.servlet.ErrorServlet;
import container.servlet.NotFoundServlet;
import container.session.HttpSession;
import container.session.SessionManager;

public class ConnectorAdapter implements HttpHandler {

    private final SessionManager sessionManager;
    private final Http11Mapper http11Mapper;

    public ConnectorAdapter(SessionManager sessionManager, Http11Mapper http11Mapper) {
        this.sessionManager = sessionManager;
        this.http11Mapper = http11Mapper;
    }

    @Override
    public void handle(Http11Request request, Http11Response response) throws Exception {
        try {
            // 1. 세션 처리 (조회 및 새 세션 발급)
            handleSession(request, response);

            // 2. 매핑 데이터 조회
            MappingData mappingData = new MappingData();
            http11Mapper.map(request.getUri(), mappingData);

            // 3. 컨텍스트 존재 확인 (방어 로직)
            if (mappingData.context == null) {
                sendError(response, 500, "No Context Found");
                return;
            }

            // 4. 실행 위임 (Context에게 실행을 맡깁니다)
            // wrapper가 null이면 Context 내부에서 404 처리를 하게 설계하는 것이 좋습니다.
            if (mappingData.wrapper == null) {
                new NotFoundServlet().service(request, response);
            } else {
                // 핵심: Wrapper를 통해 실제 서블릿 실행
                mappingData.wrapper.execute(request, response);
            }

        } catch (Exception e) {
            // 5. 500 에러 처리
            System.err.println("[Adapter] Runtime Error: " + e.getMessage());
            request.setAttribute("error_message", e.toString());
            new ErrorServlet().service(request, response);
        }
    }

    private void handleSession(Http11Request request, Http11Response response) {
        String sessionId = request.getRequestedSessionId();
        HttpSession session = null;

        if (sessionId != null) {
            session = sessionManager.findSession(sessionId);
        }

        // 세션이 없으면 새로 생성 (최초 방문자)
        if (session == null) {
            session = sessionManager.createSession();
            // 브라우저에게 세션 ID 전달 (쿠키 구워주기)
            response.addCookie("JSESSIONID", session.getId());
            System.out.println("[Adapter] New Session Created: " + session.getId());
        } else {
            session.access(); // 활동 시간 갱신
        }

        // Request 객체에 세션을 심어줌 (서블릿에서 쓸 수 있게)
        request.setSession(session);
    }

    private void sendError(Http11Response response, int status, String message) {
        response.setStatus(status, message);
        response.println("<h1>" + status + " " + message + "</h1>");
    }
}