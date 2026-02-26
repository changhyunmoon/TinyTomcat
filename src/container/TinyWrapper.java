package container;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;
import servlet.Servlet;

public class TinyWrapper {
    private final String servletClass;
    private volatile Servlet instance; // volatile: 멀티스레드 환경에서 가시성 보장

    public TinyWrapper(String servletClass) {
        this.servletClass = servletClass;
    }

    // 메서드 이름을 service로 변경하여 Context와 맞춤
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        // 1. 인스턴스가 없으면 리플렉션으로 생성 (Lazy Loading & Singleton)
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    Class<?> clazz = Class.forName(servletClass);
                    instance = (Servlet) clazz.getDeclaredConstructor().newInstance();
                    instance.init();
                    System.out.println("[TinyTomcat] Servlet Initialized: " + servletClass);
                }
            }
        }

        // 2. 실제 서블릿의 비즈니스 로직 실행
        // (이 부분은 동기화 블록 밖에서 실행되어야 여러 요청을 동시에 처리 가능합니다)
        instance.service(request, response);
    }

    public void stop() {
        if (instance != null) {
            instance.destroy();
        }
    }
}