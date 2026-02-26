package container;

import connector.protocol.HttpRequest;
import connector.protocol.HttpResponse;

public class Container {
    void invoke(HttpRequest request, HttpResponse response) throws Exception;
}
