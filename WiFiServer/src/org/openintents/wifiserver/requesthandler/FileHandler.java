package org.openintents.wifiserver.requesthandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.openintents.wifiserver.preference.OiWiFiPreferences_;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.googlecode.androidannotations.annotations.EBean;
import com.googlecode.androidannotations.annotations.RootContext;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

@EBean
public class FileHandler implements HttpRequestHandler {

    private final static String TAG = FileHandler.class.getSimpleName();

    @RootContext protected Context mContext;
    @Pref protected OiWiFiPreferences_ prefs;

    private final static Map<String, String> mimeMapping = new HashMap<String, String>() {
        private static final long serialVersionUID = -439543272217048417L;
        {
            put("js", "text/javascript");
            put("htm", "text/html");
            put("html", "text/html");
            put("png", "image/png");
            put("css", "text/css");
            put("gif", "image/gif");
        }
    };

    @Override
    public void handle(final HttpRequest request, final HttpResponse response, HttpContext context) throws HttpException, IOException {
        String path = Uri.parse(request.getRequestLine().getUri()).getPath();
        AbstractHttpEntity entity;

        if ("/".equals(path)) {
            response.setStatusCode(301);
            response.setHeader("Location", request.getRequestLine().getUri()+"index.html");
            return;
        }

        if ("/index.html".equals(path)) {
            Object authAttribute = context.getAttribute("authenticated");
            if (!(authAttribute == null || (authAttribute instanceof Boolean && ((Boolean) authAttribute).booleanValue()))) {
                response.setStatusCode(301);
                response.setHeader("Location", request.getRequestLine().getUri().replace("index.html","login.html"));
                return;
            }
        }

        if ("/login.html".equals(path)){
            final InputStream input = mContext.getAssets().open("webinterface"+path);

            String password = prefs.customPassword().get();
            String salt = password.substring(password.length()-8);

            StringBuilder result = new StringBuilder();
            String line = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            while (null != (line=reader.readLine())) {
                result.append(line.replace("$SALT$", salt));
                result.append('\n');
            }

            entity = new StringEntity(result.toString());
            entity.setContentType("text/html");
            response.setEntity(entity);
            response.setStatusCode(200);
            return;
        }

        try {
            InputStream input = mContext.getAssets().open("webinterface"+path);
            entity = new InputStreamEntity(input, -1);
            response.setEntity(entity);
            String ending = path.substring(path.lastIndexOf(".")+1);
            if (ending != null && ending.length() > 0) {
                String mime = mimeMapping.get(ending);
                if (mime != null) {
                    entity.setContentType(mime);
                }
            }

            response.setStatusCode(200);
            return;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());

            entity = new StringEntity("404 Not Found");
            entity.setContentType("text/plain");
            response.setEntity(entity);
            response.setStatusCode(404);
        }
    }
}
