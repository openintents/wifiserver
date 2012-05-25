package org.openintents.wifiserver.requesthandler;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class FallbackPageHandler implements HttpRequestHandler {

    private final static String TAG = FallbackPageHandler.class.getSimpleName();

    @Override
    public void handle(final HttpRequest request, final HttpResponse response, HttpContext context) throws HttpException, IOException {
        AbstractHttpEntity entity = new StringEntity("404 - Not Found!");
        entity.setContentType("text/plain");
        response.setEntity(entity);
        response.setStatusCode(404);
    }
}
