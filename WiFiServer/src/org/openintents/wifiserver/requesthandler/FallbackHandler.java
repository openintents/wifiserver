package org.openintents.wifiserver.requesthandler;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class FallbackHandler implements HttpRequestHandler {

    private final static String TAG = FallbackHandler.class.getSimpleName();

    @Override
    public void handle(final HttpRequest request, final HttpResponse response, HttpContext context) throws HttpException, IOException {
        Object authAttribute = context.getAttribute("authenticated");

        if (authAttribute == null || (authAttribute instanceof Boolean && ((Boolean) authAttribute).booleanValue())) {
            AbstractHttpEntity entity = new StringEntity("404 Not Found");
            entity.setContentType("text/plain");
            response.setEntity(entity);
            response.setStatusCode(404);
        } else {
            AbstractHttpEntity entity = new StringEntity("401 Unauthorized");
            entity.setContentType("text/plain");
            response.setEntity(entity);
            response.setStatusCode(401);
        }
    }
}
