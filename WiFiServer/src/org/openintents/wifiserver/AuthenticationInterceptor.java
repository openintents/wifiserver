package org.openintents.wifiserver;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.openintents.wifiserver.util.HashUtil;

/**
 * The authentication interceptor is called before a request is processed by a
 * {@link HttpRequestHandler}.
 * It checks whether the user is allowed to execute the request and sets a
 * context attribute which indicates if the check was successful or not.
 *
 * @author Stanley Förster
 *
 */
public class AuthenticationInterceptor implements HttpRequestInterceptor {

    private String TAG = AuthenticationInterceptor.class.getSimpleName();

    private final static String ATTRIBUTE_AUTHENTICATED = "authenticated";
    private final static String COOKIE_SESSIONID = "session";

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     *
     * A cookie is required to authenticate the request. The cookie's key is
     * "session" and its value is a hashed, random number.
     * If the cookie is correct the context attribute "authenticated" is set to
     * true. This attribute is used by {@link HttpRequestHandler}s to decide
     * what to do.
     */
    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        context.setAttribute(ATTRIBUTE_AUTHENTICATED, Boolean.FALSE);

        if (request.containsHeader("Cookie")) {
            Header cookieHdr = request.getHeaders("Cookie")[0];
            String cookieStr = URLDecoder.decode(cookieHdr.getValue(), "UTF-8");

            StringTokenizer tokens = new StringTokenizer(cookieStr, "=;");
            String key = null;
            String value = null;

            while (tokens.hasMoreTokens()) {
                key = tokens.nextToken().trim();
                value = tokens.nextToken().trim();

                if (key.equals(COOKIE_SESSIONID)) {
                    String sessionID = value.substring(0, value.length() - HashUtil.SALT_LENGTH);
                    String salt = value.substring(value.length() - HashUtil.SALT_LENGTH);
                    String hashedSalt = HashUtil.sha256(salt);

                    if (sessionID.equals(hashedSalt))
                        context.setAttribute("authenticated", Boolean.TRUE);
                    break;
                }
            }
        }
    }
}
