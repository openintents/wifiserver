package org.openintents.wifiserver.requesthandler;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * This handler handles requests of the form "/logout" and logs out the user
 * from the server.
 *
 * @author Stanley Förster
 *
 */
public class LogoutHandler implements HttpRequestHandler {

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     * The authentication cookie will be invalidated by setting the session-id
     * to an invalid value and the cookie's expire date to something in the
     * past.
     */
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (!"GET".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return;
        }

        response.setHeader("Set-Cookie", "session=deleted; expires=Thu, 01 Jan 1970 00:00:00 GMT");
    }

}
