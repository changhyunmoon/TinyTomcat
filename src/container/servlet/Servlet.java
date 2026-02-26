package container.servlet;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;

public interface Servlet {
    void init();
    void service(HttpRequest request, HttpResponse response) throws Exception;
    void destroy();
}
