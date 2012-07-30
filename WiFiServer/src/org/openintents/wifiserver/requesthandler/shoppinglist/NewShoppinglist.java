package org.openintents.wifiserver.requesthandler.shoppinglist;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.openintents.shopping.library.util.ShoppingUtils;
import org.openintents.wifiserver.util.URLUtil;

import android.content.Context;

public class NewShoppinglist extends ShoppinglistHandler implements
        HttpRequestHandler {

    public NewShoppinglist(Context context) {
        super(context);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (!"GET".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return;
        }

        String name = URLUtil.getParameter(request.getRequestLine().getUri(), "name");

        if (name == null || name.equals("")) {
            response.setStatusCode(400);
            return;
        }

        ShoppingUtils.getList(mContext, name);
    }
}
