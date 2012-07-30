package org.openintents.wifiserver.requesthandler.shoppinglist;

import static android.provider.BaseColumns._ID;
import static org.openintents.shopping.library.provider.ShoppingContract.Items.ACCESSED_DATE;
import static org.openintents.shopping.library.provider.ShoppingContract.Items.BARCODE;
import static org.openintents.shopping.library.provider.ShoppingContract.Items.CONTENT_URI;
import static org.openintents.shopping.library.provider.ShoppingContract.Items.CREATED_DATE;
import static org.openintents.shopping.library.provider.ShoppingContract.Items.IMAGE;
import static org.openintents.shopping.library.provider.ShoppingContract.Items.LOCATION;
import static org.openintents.shopping.library.provider.ShoppingContract.Items.MODIFIED_DATE;
import static org.openintents.shopping.library.provider.ShoppingContract.Items.NAME;
import static org.openintents.shopping.library.provider.ShoppingContract.Items.PRICE;
import static org.openintents.shopping.library.provider.ShoppingContract.Items.TAGS;
import static org.openintents.shopping.library.provider.ShoppingContract.Items.UNITS;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openintents.wifiserver.util.URLUtil;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class GetItem extends ShoppinglistHandler {

    private static final String[] PROJECTION = new String[] { _ID,
                                                              NAME,
                                                              IMAGE,
                                                              PRICE,
                                                              UNITS,
                                                              TAGS,
                                                              BARCODE,
                                                              LOCATION,
//                                                              NOTE,
                                                              CREATED_DATE,
                                                              MODIFIED_DATE,
                                                              ACCESSED_DATE };

    public GetItem(Context context) {
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
            Cursor notesCursor = mContext.getContentResolver().query(CONTENT_URI, PROJECTION, null, null, null);

            if (notesCursor == null) {
                response.setStatusCode(501);
                return;
            }

            try {
                AbstractHttpEntity entity = new StringEntity(itemsToJSONArray(notesCursor).toString());
                entity.setContentType("application/json");
                response.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Failed to create entity!", e);
                response.setStatusCode(500);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create JSON Array", e);
                response.setStatusCode(500);
            }

            notesCursor.close();
        } else {
            Cursor notesCursor = mContext.getContentResolver().query(CONTENT_URI, PROJECTION, _ID+" = ?", new String[] { id }, null);

            if (notesCursor == null) {
                response.setStatusCode(501);
                return;
            }

            if (!notesCursor.moveToFirst()) {
                response.setStatusCode(404);
                notesCursor.close();
                return;
            }

            try {
                AbstractHttpEntity entity = new StringEntity(itemToJSONObject(notesCursor).toString());
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

    protected JSONObject itemToJSONObject(int id, String name, String image, long price, String units, String tags, String barcode, String location, /*String note,*/ long created, long modified, long accessed) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(_ID, id);
        json.put(NAME, name);
        json.put(IMAGE, image);
        json.put(PRICE, price);
        json.put(UNITS, units);
        json.put(TAGS, tags);
        json.put(BARCODE, barcode);
        json.put(LOCATION, location);
//        json.put(NOTE, note);
        json.put(CREATED_DATE, created);
        json.put(MODIFIED_DATE, modified);
        json.put(ACCESSED_DATE, accessed);

        return json;
    }

    protected JSONObject itemToJSONObject(Cursor itemCursor) throws JSONException {
        return itemToJSONObject(itemCursor.getInt(   itemCursor.getColumnIndex(_ID)),
                                itemCursor.getString(itemCursor.getColumnIndex(NAME)),
                                itemCursor.getString(itemCursor.getColumnIndex(IMAGE)),
                                itemCursor.getLong(  itemCursor.getColumnIndex(PRICE)),
                                itemCursor.getString(itemCursor.getColumnIndex(UNITS)),
                                itemCursor.getString(itemCursor.getColumnIndex(TAGS)),
                                itemCursor.getString(itemCursor.getColumnIndex(BARCODE)),
                                itemCursor.getString(itemCursor.getColumnIndex(LOCATION)),
//                                itemCursor.getString(itemCursor.getColumnIndex(NOTE)),
                                itemCursor.getLong(  itemCursor.getColumnIndex(CREATED_DATE)),
                                itemCursor.getLong(  itemCursor.getColumnIndex(MODIFIED_DATE)),
                                itemCursor.getLong(  itemCursor.getColumnIndex(ACCESSED_DATE)));
    }

    protected JSONArray itemsToJSONArray(Cursor itemsCursor) throws JSONException {
        JSONArray array = new JSONArray();

        if (itemsCursor.moveToFirst())
            do {
                array.put(itemToJSONObject(itemsCursor));
            } while (itemsCursor.moveToNext());

        return array;
    }
}
