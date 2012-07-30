package org.openintents.wifiserver.requesthandler;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * This should be the base class of every request handler that requires the user to be authenticated successfully.
 *
 * @author Stanley Förster
 *
 */
public abstract class BasicAuthentiationHandler implements HttpRequestHandler {

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     * This handler method is final because then no subclass can miss a call of
     * it before executing its own implementation.
     * If the context contains an authentication attribute which is set to true,
     * the {@link #getResponse(HttpRequest, HttpResponse, HttpContext)} method
     * is called which will then provide the actual response.
     * If the authentication failed, this method only responses with 401
     * Unaothorized.
     */
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

    /**
     * This method is only called if the authentication was successful.
     * It can be used like the original
     * {@link #handle(HttpRequest, HttpResponse, HttpContext)} method.
     */
    protected abstract void getResponse(HttpRequest request, HttpResponse response, HttpContext context);
}
