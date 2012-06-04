package org.openintents.wifiserver.requesthandler;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public abstract class BasicAuthentiationHandler implements HttpRequestHandler {

    @Override
    public final void handle(final HttpRequest request, final HttpResponse response, HttpContext context) throws HttpException, IOException {
        Object authAttribute = context.getAttribute("authenticated");

        if (authAttribute == null || (authAttribute instanceof Boolean && ((Boolean) authAttribute).booleanValue())) {
            getResponse(request, response, context);
            return;
        }

        AbstractHttpEntity entity = new StringEntity("401 Unauthorized");
        entity.setContentType("text/plain");
        response.setEntity(entity);
        response.setStatusCode(401);
    }

    protected abstract void getResponse(HttpRequest request, HttpResponse response, HttpContext context);
}
