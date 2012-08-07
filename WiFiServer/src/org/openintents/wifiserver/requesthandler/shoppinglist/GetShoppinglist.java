package org.openintents.wifiserver.requesthandler.shoppinglist;

import static android.provider.BaseColumns._ID;
import static org.openintents.shopping.library.provider.ShoppingContract.Lists.NAME;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openintents.shopping.library.provider.ShoppingContract;
import org.openintents.wifiserver.util.URLUtil;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class GetShoppinglist extends ShoppinglistHandler {

    private static final String[] PROJECTION = new String[] { _ID, NAME };

    public GetShoppinglist(Context context) {
        super(context);
    }

    @Override
    public void getResponse(HttpRequest request, HttpResponse response, HttpContext context) {
        if (!"GET".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return;
        }

        String id = URLUtil.getParameter(request.getRequestLine().getUri(), "id");

        if (id == null) {
            Cursor listsCursor = mContext.getContentResolver().query(ShoppingContract.Lists.CONTENT_URI, PROJECTION, null, null, null);

            if (listsCursor == null) {
                response.setStatusCode(501);
                return;
            }

            try {
                AbstractHttpEntity entity = new StringEntity(listsToJSONArray(listsCursor).toString());
                entity.setContentType("application/json");
                response.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Failed to create entity!", e);
                response.setStatusCode(500);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create JSON Array", e);
                response.setStatusCode(500);
            }

            listsCursor.close();
        } else {
            Cursor listsCursor = mContext.getContentResolver().query(ShoppingContract.Lists.CONTENT_URI, PROJECTION, _ID+" = ?", new String[] { id }, null);

            if (listsCursor == null) {
                response.setStatusCode(501);
                return;
            }

            if (!listsCursor.moveToFirst()) {
                response.setStatusCode(404);
                listsCursor.close();
                return;
            }

            try {
                AbstractHttpEntity entity = new StringEntity(listToJSONObject(listsCursor).toString());
                entity.setContentType("application/json");
                response.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Failed to create entity!", e);
                response.setStatusCode(500);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create JSON Object", e);
                response.setStatusCode(500);
            }
        }
    }

    protected JSONObject listToJSONObject(int id, String name) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(_ID, id);
        json.put(NAME, name);

        return json;
    }

    protected JSONObject listToJSONObject(Cursor listCursor) throws JSONException {
        return listToJSONObject(listCursor.getInt(listCursor.getColumnIndex(_ID)),
                        listCursor.getString(listCursor.getColumnIndex(NAME)));
    }

    protected JSONArray listsToJSONArray(Cursor listsCursor) throws JSONException {
        JSONArray array = new JSONArray();

        if (listsCursor.moveToFirst())
            do {
                array.put(listToJSONObject(listsCursor));
            } while (listsCursor.moveToNext());

        return array;
    }
}
