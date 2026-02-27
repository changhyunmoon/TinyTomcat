package container;

import connector.http11.dto.Http11Request;
import connector.http11.dto.Http11Response;

public interface TinyServlet {
    void init();
    void service(Http11Request request, Http11Response response) throws Exception;
    void destroy();
}