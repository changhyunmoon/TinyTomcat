package container.session;

import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class HttpSession {

    private final String id; // 세션 고유 ID (JSESSIONID)
    private final long creationTime; // 생성 시간
    private long lastAccessedTime; // 마지막 접근 시간
    private int maxInactiveInterval = 30 * 60; // 세션 유지 시간 (초 단위, 기본 30분)
    private boolean isValid = true; // 세션 유효 여부

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public HttpSession(String id) {
        this.id = id;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = creationTime;
    }

    public Object getAttribute(String name) {
        checkValid();
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        checkValid();
        if (value == null) {
            removeAttribute(name);
        } else {
            attributes.put(name, value);
        }
    }

    public void removeAttribute(String name) {
        checkValid();
        attributes.remove(name);
    }

    public Enumeration<String> getAttributeNames() {
        checkValid();
        return new Vector<>(attributes.keySet()).elements();
    }

    public String getId() {
        return id;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void access() {
        this.lastAccessedTime = System.currentTimeMillis();
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public boolean isExpired() {
        if (!isValid) return true;
        long now = System.currentTimeMillis();
        return (now - lastAccessedTime) > (maxInactiveInterval * 1000L);
    }

    public void invalidate() {
        this.isValid = false;
        attributes.clear();
    }

    private void checkValid() {
        if (!isValid) {
            throw new IllegalStateException("세션이 이미 만료되었습니다.");
        }
    }

    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }
}