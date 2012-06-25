package org.openintents.wifiserver;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

public class AuthenticationInterceptor implements HttpRequestInterceptor {

    private String TAG = AuthenticationInterceptor.class.getSimpleName();

    private final static String KEY_AUTHENTICATED = "authenticated";

    public AuthenticationInterceptor(String password) {
    }

    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        context.setAttribute("authenticated", Boolean.FALSE);

        if (request.containsHeader("Cookie")) {
            Header cookieHdr = request.getHeaders("Cookie")[0];
            String cookieStr = URLDecoder.decode(cookieHdr.getValue(), "UTF-8");

            StringTokenizer tokens = new StringTokenizer(cookieStr, "=;");
            String key = null;
            String value = null;
            while (tokens.hasMoreTokens()) {
                key = tokens.nextToken();
                value = tokens.nextToken();

                if (key.equals(KEY_AUTHENTICATED) && value.equals("true")) {
                    context.setAttribute("authenticated", Boolean.TRUE);
                    break;
                }
            }

        }
    }
}
