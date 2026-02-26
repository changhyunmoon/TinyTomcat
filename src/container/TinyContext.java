package container;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;

import java.util.HashMap;
import java.util.Map;

public class TinyContext {
    // URL 패턴 : Wrapper(서블릿 관리자) 매핑 저장소
    private final Map<String, TinyWrapper> childWrappers = new HashMap<>();
    private final Mapper mapper;

    public TinyContext() {
        this.mapper = new Mapper(this);
    }

    // 서버 시작 시 서블릿 등록용
    public void addServlet(String urlPattern, String className) {
        childWrappers.put(urlPattern, new TinyWrapper(className));
    }

    // Mapper가 호출할 검색 메서드
    public TinyWrapper findWrapper(String path) {
        return childWrappers.get(path);
    }

    // 실제 요청이 들어왔을 때 실행되는 지점
    public void dispatch(HttpRequest request, HttpResponse response) {
        TinyWrapper wrapper = mapper.map(request);

        if (wrapper != null) {
            try {
                // Checked Exception이 발생하는 지점
                wrapper.service(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                send500(response, e); // 에러 발생 시 500 응답 처리
            }
        } else {
            send404(response);
        }
    }

    private void send404(HttpResponse response) {
        // 간단한 콘솔 로그 또는 404 응답 로직
        System.out.println("[TinyTomcat] 404 Not Found: 해당 경로에 매핑된 서블릿이 없습니다.");
    }

    private void send500(HttpResponse response, Exception e) {
        try {
            response.setStatus(500);
            response.setHeader("Content-Type", "text/plain; charset=utf-8");
            String errorMessage = "Internal Server Error: " + e.getMessage();
            response.setBody(errorMessage.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}