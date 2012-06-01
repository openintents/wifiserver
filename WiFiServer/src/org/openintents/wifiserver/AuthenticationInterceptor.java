package org.openintents.wifiserver;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import android.net.Uri;
import android.util.Log;

public class AuthenticationInterceptor implements HttpRequestInterceptor {

    private String TAG = AuthenticationInterceptor.class.getSimpleName();
    
    private String password;
    
    public AuthenticationInterceptor(String password) {
        this.password = password;
    }
    
    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        Uri uri = Uri.parse(request.getRequestLine().getUri());
        String sid = null;
        
        Object authAttribute = context.getAttribute("authenticated");
        boolean authenticated = false;
        
        if (authAttribute instanceof Boolean) {            
            authenticated = ((Boolean)authAttribute).booleanValue();
        }
        
        Log.d(TAG, "authenticated: "+context.getAttribute("authenticated"));
        
        if (authenticated || (sid = uri.getQueryParameter("sid")) != null && sid.equals(password)) {
            context.setAttribute("authenticated", Boolean.TRUE);
            return;
        }
        
        context.setAttribute("authenticated", Boolean.FALSE);
    }
}
