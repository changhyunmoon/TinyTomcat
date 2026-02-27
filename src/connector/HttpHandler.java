package connector;

import connector.http11.dto.Http11Request;
import connector.http11.dto.Http11Response;

public interface HttpHandler {
    void handle(Http11Request request, Http11Response response) throws Exception;
}
