package servlet;

/*
    Http 전용 servlet
 */

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;

public abstract class HttpServlet implements Servlet {
    @Override
    public void init() {}

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        if ("GET".equals(request.getMethod())) {
            doGet(request, response);
        } else if ("POST".equals(request.getMethod())) {
            doPost(request, response);
        }
    }

    protected void doGet(HttpRequest request, HttpResponse response) throws Exception {
        response.setStatus(405);
    }

    protected void doPost(HttpRequest request, HttpResponse response) throws Exception {
        response.setStatus(405);
    }

    @Override
    public void destroy() {}
}
