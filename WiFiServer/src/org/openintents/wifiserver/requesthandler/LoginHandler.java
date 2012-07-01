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
import org.openintents.wifiserver.preference.OiWiFiPreferences_;
import org.openintents.wifiserver.util.HashUtil;
import org.openintents.wifiserver.util.URLEncodedUtils;

import android.util.Log;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

@EBean
public class LoginHandler implements HttpRequestHandler {

    private final static String TAG = LoginHandler.class.getSimpleName();

    @Pref protected OiWiFiPreferences_ prefs;

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (request instanceof BasicHttpEntityEnclosingRequest) {
            HttpEntity postEntity = ((BasicHttpEntityEnclosingRequest)request).getEntity();

            List<NameValuePair> postParams;
            try {
                postParams = URLEncodedUtils.parse(postEntity);
            } catch (IOException e) {
                Log.e(TAG, "Failed to parse parameters!", e);
                response.setStatusCode(500);
                return;
            }

            for (NameValuePair nvp : postParams) {
                if ("password".equals(nvp.getName())) {

                    String actualPassword = nvp.getValue();

                    String hashedPassword = prefs.customPassword().get();
                    String expectedPassword = hashedPassword.substring(0, hashedPassword.length() - HashUtil.SALT_LENGTH);

                    if (actualPassword != null && expectedPassword.equals(actualPassword)) {
                        String sessionSalt = HashUtil.generateSalt();
                        String sessionID = HashUtil.sha256(sessionSalt)+sessionSalt;
                        response.addHeader("Set-Cookie", URLEncoder.encode("session="+sessionID, "UTF-8"));
                    }

                    break;
                }
            }
        }

        response.setStatusCode(301);
        response.setHeader("Location", "index.html");
    }
}