package org.openintents.wifiserver.requesthandler.shoppinglist;

import static android.provider.BaseColumns._ID;

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
import org.openintents.shopping.library.provider.ShoppingContract.ContainsFull;
import org.openintents.shopping.library.provider.ShoppingContract.Items;
import org.openintents.wifiserver.util.URLUtil;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class GetItem extends ShoppinglistHandler {

    private static final String[] PROJECTION_ITEMS = new String[] { Items._ID,
                                                                    Items.NAME,
                                                                    Items.IMAGE,
                                                                    Items.PRICE,
                                                                    Items.UNITS,
                                                                    Items.TAGS,
                                                                    Items.BARCODE,
                                                                    Items.LOCATION,
//                                                                  Items.NOTE,
                                                                  };

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
        String list = URLUtil.getParameter(request.getRequestLine().getUri(), "list");

        if (id == null) {
            if (list != null) {
                Cursor itemsListCursor = mContext.getContentResolver().query(ContainsFull.CONTENT_URI,
                        new String[] { ContainsFull.ITEM_ID,
                                       ContainsFull.ITEM_NAME,
                                       ContainsFull.ITEM_IMAGE,
                                       ContainsFull.ITEM_PRICE,
                                       ContainsFull.ITEM_UNITS,
                                       ContainsFull.ITEM_TAGS,
//                                       ContainsFull.BARCODE,
//                                       ContainsFull.LOCATION,
                                       ContainsFull.LIST_ID,
                                       ContainsFull.PRIORITY,
                                       ContainsFull.QUANTITY,
                                       ContainsFull.STATUS
                }, ContainsFull.LIST_ID + " = ?", new String[] { list }, null);

                try {
                    JSONArray array = new JSONArray();

                    if (itemsListCursor.moveToFirst())
                        do {
                            JSONObject object = new JSONObject();

                            object.put(ContainsFull.ITEM_ID,    itemsListCursor.getLong(  itemsListCursor.getColumnIndex(ContainsFull.ITEM_ID)));
                            object.put(ContainsFull.ITEM_NAME,  itemsListCursor.getString(itemsListCursor.getColumnIndex(ContainsFull.ITEM_NAME)));
                            object.put(ContainsFull.ITEM_IMAGE, itemsListCursor.getString(itemsListCursor.getColumnIndex(ContainsFull.ITEM_IMAGE)));
                            object.put(ContainsFull.ITEM_PRICE, itemsListCursor.getInt(   itemsListCursor.getColumnIndex(ContainsFull.ITEM_PRICE)));
                            object.put(ContainsFull.ITEM_UNITS, itemsListCursor.getString(itemsListCursor.getColumnIndex(ContainsFull.ITEM_UNITS)));
                            object.put(ContainsFull.ITEM_TAGS,  itemsListCursor.getString(itemsListCursor.getColumnIndex(ContainsFull.ITEM_TAGS)));
//                            object.put(ContainsFull.BARCODE,    itemsListCursor.getString(itemsListCursor.getColumnIndex(ContainsFull.BARCODE)));
//                            object.put(ContainsFull.LOCATION,   itemsListCursor.getString(itemsListCursor.getColumnIndex(ContainsFull.LOCATION)));
                            object.put(ContainsFull.LIST_ID,    itemsListCursor.getLong(  itemsListCursor.getColumnIndex(ContainsFull.LIST_ID)));
                            object.put(ContainsFull.PRIORITY,   itemsListCursor.getLong(  itemsListCursor.getColumnIndex(ContainsFull.PRIORITY)));
                            object.put(ContainsFull.QUANTITY,   itemsListCursor.getString(itemsListCursor.getColumnIndex(ContainsFull.QUANTITY)));
                            object.put(ContainsFull.STATUS,     itemsListCursor.getLong(  itemsListCursor.getColumnIndex(ContainsFull.STATUS)));

                            array.put(object);
                        } while (itemsListCursor.moveToNext());

                    AbstractHttpEntity entity = new StringEntity(array.toString());
                    entity.setContentType("application/json");
                    response.setEntity(entity);
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Failed to create entity!", e);
                    response.setStatusCode(500);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to create JSON Array", e);
                    response.setStatusCode(500);
                }

                itemsListCursor.close();

//                Cursor containsCursor = mContext.getContentResolver().query(Contains.CONTENT_URI, PROJECTION_CONTAINS, Contains.LIST_ID+" = ?", new String[] { list }, Contains.ITEM_ID+" ASC");
//                Cursor itemsCursor = mContext.getContentResolver().query(Items.CONTENT_URI, PROJECTION_ITEMS, null, null, _ID+" ASC");
//
//                CursorJoiner joiner = new CursorJoiner(containsCursor, new String[] { Contains.ITEM_ID }, itemsCursor, new String[] { _ID });
//
//                try {
//                    JSONArray array = new JSONArray();
//
//                    for (CursorJoiner.Result joinerResult : joiner) {
//                        switch (joinerResult) {
//                            case LEFT:
//                                // Log.d(Left row: output cursor row());
//                                break;
//                            case RIGHT:
//                                // Log.d(Right row: output cursor row());
//                                break;
//                            case BOTH:
//                                JSONObject json = itemToJSONObject(itemsCursor);
//                                json.put(Contains.PRIORITY, containsCursor.getInt(containsCursor.getColumnIndex(Contains.PRIORITY)));
//                                json.put(Contains.QUANTITY, containsCursor.getString(containsCursor.getColumnIndex(Contains.QUANTITY)));
//                                json.put(Contains.STATUS, containsCursor.getInt(containsCursor.getColumnIndex(Contains.STATUS)));
//
//                                array.put(json);
//                                break;
//                        }
//                    }
//
//                    AbstractHttpEntity entity = new StringEntity(itemsToJSONArray(itemsCursor).toString());
//                    entity.setContentType("application/json");
//                    response.setEntity(entity);
//                } catch (UnsupportedEncodingException e) {
//                    Log.e(TAG, "Failed to create entity!", e);
//                    response.setStatusCode(500);
//                } catch (JSONException e) {
//                    Log.e(TAG, "Failed to create JSON Array", e);
//                    response.setStatusCode(500);
//                }
//
//                itemsCursor.close();
//                containsCursor.close();
            } else {
                Cursor notesCursor = mContext.getContentResolver().query(Items.CONTENT_URI, PROJECTION_ITEMS, null, null, null);

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
            }
        } else {
            Cursor itemsCursor = mContext.getContentResolver().query(Items.CONTENT_URI, PROJECTION_ITEMS, _ID+" = ?", new String[] { id }, null);

            if (itemsCursor == null) {
                response.setStatusCode(501);
                return;
            }

            if (!itemsCursor.moveToFirst()) {
                response.setStatusCode(404);
                itemsCursor.close();
                return;
            }

            try {
                AbstractHttpEntity entity = new StringEntity(itemToJSONObject(itemsCursor).toString());
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

    protected JSONObject itemToJSONObject(long id, String name, String image, long price, String units, String tags, String barcode, String location/*, String note,*/) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Items._ID, id);
        json.put(Items.NAME, name);
        json.put(Items.IMAGE, image);
        json.put(Items.PRICE, price);
        json.put(Items.UNITS, units);
        json.put(Items.TAGS, tags);
        json.put(Items.BARCODE, barcode);
        json.put(Items.LOCATION, location);
//        json.put(NOTE, note);
        return json;
    }

    protected JSONObject itemToJSONObject(Cursor itemCursor) throws JSONException {
        return itemToJSONObject(itemCursor.getLong(  itemCursor.getColumnIndex(Items._ID)),
                                itemCursor.getString(itemCursor.getColumnIndex(Items.NAME)),
                                itemCursor.getString(itemCursor.getColumnIndex(Items.IMAGE)),
                                itemCursor.getLong(  itemCursor.getColumnIndex(Items.PRICE)),
                                itemCursor.getString(itemCursor.getColumnIndex(Items.UNITS)),
                                itemCursor.getString(itemCursor.getColumnIndex(Items.TAGS)),
                                itemCursor.getString(itemCursor.getColumnIndex(Items.BARCODE)),
                                itemCursor.getString(itemCursor.getColumnIndex(Items.LOCATION))
//                                itemCursor.getString(itemCursor.getColumnIndex(NOTE)),
                                );
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
