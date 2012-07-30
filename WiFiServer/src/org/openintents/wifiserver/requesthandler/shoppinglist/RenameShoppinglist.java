package org.openintents.wifiserver.requesthandler.shoppinglist;

import static org.openintents.shopping.library.provider.ShoppingContract.Lists.CONTENT_URI;
import static org.openintents.shopping.library.provider.ShoppingContract.Lists.MODIFIED_DATE;
import static org.openintents.shopping.library.provider.ShoppingContract.Lists.NAME;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.openintents.wifiserver.util.URLUtil;

import android.content.ContentValues;
import android.content.Context;

public class RenameShoppinglist extends ShoppinglistHandler {

    public RenameShoppinglist(Context context) {
        super(context);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (!"GET".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return;
        }

        String oldName = URLUtil.getParameter(request.getRequestLine().getUri(), "oldname");
        String newName = URLUtil.getParameter(request.getRequestLine().getUri(), "newname");

        if (oldName == null || newName == null || newName.equals("")) {
            response.setStatusCode(400);
            return;
        }

        ContentValues values = new ContentValues();
        values.put(NAME, newName);
        values.put(MODIFIED_DATE, Long.valueOf(System.currentTimeMillis()));

        if (0 == mContext.getContentResolver().update(CONTENT_URI, values, NAME+" = ?", new String[] { oldName })) {
            response.setStatusCode(400);
        }
    }
}
