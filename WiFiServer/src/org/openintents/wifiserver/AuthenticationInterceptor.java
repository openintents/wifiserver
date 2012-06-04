package org.openintents.wifiserver;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.openintents.wifiserver.util.URLUtil;

public class AuthenticationInterceptor implements HttpRequestInterceptor {

    private String TAG = AuthenticationInterceptor.class.getSimpleName();
    
    private String password;
    
    public AuthenticationInterceptor(String password) {
        this.password = password;
    }
    
    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        String sid = URLUtil.getParameter(request.getRequestLine().getUri(), "sid");
        
        if (sid != null && sid.equals(password)) {
            context.setAttribute("authenticated", Boolean.TRUE);
            return;
        }
        
        context.setAttribute("authenticated", Boolean.FALSE);
    }
}
