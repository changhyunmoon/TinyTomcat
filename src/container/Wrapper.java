package container;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;
import servlet.Servlet;

public class Wrapper {
    private final String servletClass;
    private Servlet instance;

    public Wrapper(String servletClass) {
        this.servletClass = servletClass;
    }

    public synchronized void handle(HttpRequest request, HttpResponse response) throws Exception {
        // 1. 인스턴스가 없으면 리플렉션으로 생성 (싱글톤)
        if (instance == null) {
            Class<?> clazz = Class.forName(servletClass);
            instance = (Servlet) clazz.getDeclaredConstructor().newInstance();
            instance.init(); // 직접 만드신 init() 호출
        }

        // 2. 서비스 실행
        instance.service(request, response);
    }

    public void stop() {
        if (instance != null) {
            instance.destroy();
        }
    }
}
