package org.openintents.wifiserver;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.openintents.wifiserver.util.HashUtil;

public class AuthenticationInterceptor implements HttpRequestInterceptor {

    private String TAG = AuthenticationInterceptor.class.getSimpleName();

    private final static String ATTRIBUTE_AUTHENTICATED = "authenticated";
    private final static String COOKIE_SESSIONID = "session";

    public AuthenticationInterceptor(String password) {
    }

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
                key = tokens.nextToken();
                value = tokens.nextToken();

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
