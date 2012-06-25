package org.openintents.wifiserver.requesthandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.openintents.wifiserver.util.URLEncodedUtils;

import android.util.Log;

public class LoginHandler implements HttpRequestHandler {

    private final static String TAG = LoginHandler.class.getSimpleName();

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (request instanceof BasicHttpEntityEnclosingRequest) {
            HttpEntity postEntity = ((BasicHttpEntityEnclosingRequest)request).getEntity();

            String password = null;

            List<NameValuePair> postParams;
            try {
                postParams = URLEncodedUtils.parse(postEntity);
            } catch (IOException e) {
                Log.e(TAG, "Failed to parse parameters!", e);
                response.setStatusCode(500);
                return;
            }

            for (NameValuePair nvp : postParams) {
                Log.d(TAG, "("+nvp.getName()+"|"+nvp.getValue()+")");
                if ("password".equals(nvp.getName()))
                    password = nvp.getValue();
            }

            if (password != null && "12345".equals(password)) {
                response.addHeader("Set-Cookie", URLEncoder.encode("authenticated=true", "UTF-8"));
            }
        }

        response.setStatusCode(301);
        response.setHeader("Location", "index.html");
    }
}