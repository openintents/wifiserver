package org.openintents.wifiserver.requesthandler;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.res.AssetManager;

public class HttpMethodTestHandler implements HttpRequestHandler {

    private AssetManager mAssetManager;
    
    public HttpMethodTestHandler(AssetManager assets) {
        this.mAssetManager = assets;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        AbstractHttpEntity entity = null;
        
        if (request.getRequestLine().getUri().matches("/test/?")) {
            entity = new InputStreamEntity(mAssetManager.open("test.html"), -1);
        } else if (request.getRequestLine().getUri().matches("/test/result.*")) {
            entity = new StringEntity(new String("<html>" +
            		"<body>" +
            		"Successful received request with method " +
            		request.getRequestLine().getMethod() +
            		"<br /><br />" +
            		"<a href='/test'>" +
            		"Back" +
            		"</a>" +
            		"</body>" +
            		"</html>"));
        } else {
            entity = new StringEntity("<html>" +
            		"<body>" +
            		"404 - Not Found!" +
            		"<br /><br />" +
            		request.getRequestLine().getUri() +
            		"</body>" +
            		"</html>");
        }

        entity.setContentType("text/html");
        response.setEntity(entity);
    }

}
