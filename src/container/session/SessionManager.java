package container.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    private final SessionCleaner cleaner;

    public SessionManager() {
        // 1분 주기로 청소하는 클리너 생성 및 시작
        this.cleaner = new SessionCleaner(sessions, 1);
        this.cleaner.start();
    }

    public HttpSession createSession() {
        String sessionId = UUID.randomUUID().toString();
        HttpSession session = new HttpSession(sessionId);
        sessions.put(sessionId, session);
        return session;
    }

    public HttpSession findSession(String sessionId) {
        if (sessionId == null) return null;
        return sessions.get(sessionId);
    }

    public void removeSession(String sessionId) {
        if (sessionId != null) {
            HttpSession session = sessions.remove(sessionId);
            if (session != null) session.invalidate();
        }
    }

    public void stop() {
        cleaner.stop();
    }
}