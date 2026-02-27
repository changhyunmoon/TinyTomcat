package connector;

import connector.http11.Http11Request;
import connector.http11.Http11Response;
import container.TinyContext;
import container.session.HttpSession;
import container.session.SessionManager;

public class ConnectorAdapter implements HttpHandler {

    private final TinyContext context;
    private final SessionManager sessionManager;

    // 생성자를 통해 의존성을 주입받습니다 (컴파일 에러 해결)
    public ConnectorAdapter(TinyContext context, SessionManager sessionManager) {
        this.context = context;
        this.sessionManager = sessionManager;
    }

    @Override
    public void handle(Http11Request request, Http11Response response) throws Exception {
        // 1. 요청에서 세션 ID 추출 (이미 Request 파싱 단계에서 완료됨)
        String sessionId = request.getRequestedSessionId();

        // 2. 세션 매칭 로직 (HttpRequest 객체는 이 로직을 몰라도 됩니다)
        if (sessionId != null) {
            HttpSession session = sessionManager.findSession(sessionId);
            if (session != null) {
                session.access(); // 마지막 접근 시간 갱신
                // 여기서 세션을 별도의 저장소에 담거나,
                // Context가 필요로 하는 형태로 전달합니다.
            }
        }

        // 3. 컨테이너 실행
        if (context != null) {
            //context.execute(request, response);
        }
    }
}