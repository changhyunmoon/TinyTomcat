package container.session;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 만료된 세션을 주기적으로 감시하고 삭제하는 독립적인 청소부 객체
 */
public class SessionCleaner {

    private final Map<String, HttpSession> sessions;
    private final ScheduledExecutorService scheduler;
    private final long intervalMinutes;

    public SessionCleaner(Map<String, HttpSession> sessions, long intervalMinutes) {
        this.sessions = sessions;
        this.intervalMinutes = intervalMinutes;
        // 단일 스레드 스케줄러 생성
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread t = new Thread(runnable, "Session-Cleaner-Thread");
            t.setDaemon(true); // 서버 종료 시 함께 종료
            return t;
        });
    }

    /**
     * 청소 시작
     */
    public void start() {
        System.out.println("[SessionCleaner] 세션 감시 스레드를 시작합니다. (주기: " + intervalMinutes + "분)");

        // 일정 주기마다 반복 실행
        scheduler.scheduleAtFixedRate(this::clean, intervalMinutes, intervalMinutes, TimeUnit.MINUTES);
    }

    /**
     * 실제 청소 로직
     */
    private void clean() {
        if (sessions.isEmpty()) return;

        int beforeSize = sessions.size();

        // 만료된 세션들만 찾아서 제거
        // removeIf는 ConcurrentHashMap과 함께 사용 시 안전하게 동작합니다.
        sessions.entrySet().removeIf(entry -> {
            HttpSession session = entry.getValue();
            if (session.isExpired()) {
                session.invalidate(); // 세션 내부 상태 변경 및 attribute 클리어
                return true;
            }
            return false;
        });

        int afterSize = sessions.size();
        int removedCount = beforeSize - afterSize;

        if (removedCount > 0) {
            System.out.println("[SessionCleaner] 청소 완료. (삭제됨: " + removedCount + ", 현재 활성 세션: " + afterSize + ")");
        }
    }

    /**
     * 청소 중지
     */
    public void stop() {
        System.out.println("[SessionCleaner] 세션 감시 스레드를 중지합니다.");
        scheduler.shutdown();
    }
}