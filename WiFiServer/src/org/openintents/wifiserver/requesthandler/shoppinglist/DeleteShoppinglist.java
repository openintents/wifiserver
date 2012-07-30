package org.openintents.wifiserver.requesthandler.shoppinglist;

import static android.provider.BaseColumns._ID;
import static org.openintents.shopping.library.provider.ShoppingContract.Lists.CONTENT_URI;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.openintents.shopping.library.util.ShoppingUtils;
import org.openintents.wifiserver.util.URLUtil;

import android.content.Context;
import android.database.Cursor;

public class DeleteShoppinglist extends ShoppinglistHandler {

    public DeleteShoppinglist(Context context) {
        super(context);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (!"GET".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return;
        }

        String id = URLUtil.getParameter(request.getRequestLine().getUri(), "id");

        if (id == null) {
            Cursor listsCursor = mContext.getContentResolver().query(CONTENT_URI, new String[] { _ID }, null, null, null);

            if (listsCursor == null)
                return;

            if (listsCursor.moveToFirst())
                do {
                    deleteList(listsCursor.getString(listsCursor.getColumnIndex(_ID)));
                } while (listsCursor.moveToNext());

            listsCursor.close();
        } else {
            deleteList(id);
        }
    }

    private void deleteList(String id) {
        ShoppingUtils.deleteList(mContext, id);
    }
}
