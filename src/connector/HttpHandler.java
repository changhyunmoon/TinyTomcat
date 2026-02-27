package connector;

import connector.http11.Http11Request;
import connector.http11.Http11Response;

public interface HttpHandler {
    void handle(Http11Request request, Http11Response response) throws Exception;
}
