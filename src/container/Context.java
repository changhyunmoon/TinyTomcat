package container;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;

import java.util.HashMap;
import java.util.Map;

public class Context implements Container{
    private final String contextPath; // 웹 상의 경로 (예: /blog)
    private final String docBase;     // 실제 파일 저장 경로 (예: C:/webapps/blog)
    private final Map<String, Wrapper> servletMappings = new HashMap<>();

    public Context(String contextPath, String docBase) {
        this.contextPath = contextPath;
        this.docBase = docBase;
    }

    public void addServlet(String urlPattern, Wrapper wrapper) {
        servletMappings.put(urlPattern, wrapper);
    }

    @Override
    public void invoke(HttpRequest request, HttpResponse response) throws Exception {
        String uri = request.getUri();

        // ContextPath를 제외한 순수 서블릿 경로 추출
        String servletPath = uri.substring(contextPath.equals("/") ? 0 : contextPath.length());
        if (servletPath.isEmpty()) servletPath = "/";

        Wrapper wrapper = servletMappings.get(servletPath);

        if (wrapper != null) {
            wrapper.invoke(request, response);
        } else {
            // 여기에 나중에 'docBase'에서 파일을 찾는 로직(DefaultServlet)이 들어갑니다.
            response.setStatus(404);
            response.setBody("404 Not Found in " + contextPath);
        }
    }
}
