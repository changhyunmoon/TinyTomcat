package container;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TinyContext {
    // 경로와 Wrapper 매핑
    private final Map<String, Wrapper> mapping = new ConcurrentHashMap<>();

    // 서블릿 등록 메서드
    public void addServlet(String urlPattern, String servletClass) {
        mapping.put(urlPattern, new Wrapper(servletClass));
    }

    public void dispatch(HttpRequest request, HttpResponse response) {
        String uri = request.getUri();
        Wrapper wrapper = mapping.get(uri);

        try {
            if (wrapper != null) {
                wrapper.handle(request, response);
            } else {
                send404(response);
            }
            // 모든 처리가 끝나면 Http11Response의 send() 호출
            response.send();
        } catch (Exception e) {
            e.printStackTrace();
            send500(response);
        }
    }

    private void send404(HttpResponse response) {
        response.setStatus(404);
        response.setBody("<h1>404 Not Found</h1>");
    }

    private void send500(HttpResponse response) {
        response.setStatus(500);
        response.setBody("<h1>500 Internal Error</h1>");
    }
}