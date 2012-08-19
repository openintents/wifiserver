package org.openintents.wifiserver.requesthandler.shoppinglist;

import static android.provider.BaseColumns._ID;

import java.io.UnsupportedEncodingException;

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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Handler which is used to retrieve all or a specific item. It handles requests of the form "/shoppinglist/list/get".
 *
 * @author Stanley Förster
 *
 */
public class GetItem extends ShoppinglistHandler {

    private static final String[] PROJECTION_ITEMS = new String[] { Items._ID,
                                                                    Items.NAME,
                                                                    Items.PRICE,
                                                                    Items.UNITS,
                                                                    Items.TAGS
                                                                  };

    private static final String[] PROJECTION_CONTAINS = new String[] { ContainsFull.ITEM_ID,
                                                                       ContainsFull.ITEM_NAME,
                                                                       ContainsFull.ITEM_PRICE,
                                                                       ContainsFull.ITEM_UNITS,
                                                                       ContainsFull.ITEM_TAGS,
                                                                       ContainsFull.LIST_ID,
                                                                       ContainsFull.PRIORITY,
                                                                       ContainsFull.QUANTITY,
                                                                       ContainsFull.STATUS
                                                                     };

    /**
     * Creates a new request handler.
     *
     * @param context The application's context to access the necessary content providers.
     */
    public GetItem(Context context) {
        super(context);
    }

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     *
     * This method deletes an item. The required HTTP method is GET.
     *
     * To specify a special item, the <code>id</code> and <code>list</code> parameters can be used:
     * <ul>
     * <li><code>id</code> specifies the item, that should be returned.</li>
     * <li><code>list</code> specifies the list, that contains the item.</li>
     * </ul>
     * If no parameter is given, all items will be returned. If only the <code>list</code> parameter is present, all items of this list will be returned.
     *
     * <p>
     * Status Codes:
     * <ul>
     * <li>405: if HTTP method is not GET</li>
     * <li>501: if OI Shoppinglist app is not installed on the device.</li>
     * <li>500: if something went wrong during response creation.</li>
     * </ul>
     * </p>
     */
    @Override
    public void getResponse(HttpRequest request, HttpResponse response, HttpContext context) {
        if (!"GET".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return;
        }

        String id = URLUtil.getParameter(request.getRequestLine().getUri(), "id");
        String list = URLUtil.getParameter(request.getRequestLine().getUri(), "list");
        Cursor cursor = null;
        Object jsonResult = null;

        try {
            if (id == null && list == null) {
                cursor = query(Items.CONTENT_URI, PROJECTION_ITEMS, null, null, null);
                jsonResult = itemsToJSONArray(cursor);
            }

            if (id == null && list != null) {
                cursor = query(ContainsFull.CONTENT_URI, PROJECTION_CONTAINS, ContainsFull.LIST_ID + " = ?", new String[] { list }, null);
                jsonResult = containsToJSONArray(cursor);
            }

            if (id != null && list != null) {
                cursor = query(ContainsFull.CONTENT_URI, PROJECTION_CONTAINS, ContainsFull.LIST_ID + " = ? AND " + ContainsFull.ITEM_ID + " = ?", new String[] { list, id }, null);
                if (cursor.getCount() == 0) {
                    response.setStatusCode(404);
                    return;
                }
                jsonResult = containsToJSONObject(cursor);
            }

            if (id != null && list == null) {
                cursor = query(Items.CONTENT_URI, PROJECTION_ITEMS, _ID+" = ?", new String[] { id }, null);
                if (cursor.getCount() == 0) {
                    response.setStatusCode(404);
                    return;
                }
                jsonResult = itemToJSONObject(cursor);
            }

            AbstractHttpEntity entity = new StringEntity(jsonResult.toString());
            entity.setContentType("application/json");
            response.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to create entity!", e);
            response.setStatusCode(500);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create JSON Object", e);
            response.setStatusCode(500);
        } catch (UnsupportedOperationException e) {
            Log.e(TAG, e.getMessage(), e);
            response.setStatusCode(501);
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    /**
     * <p>
     * Wrapper for the {@link ContentResolver#query(Uri, String[],
     * String, String[], String)} method which throws an exception if the
     * necessary URI is not available.
     * This behavior simplifies the handling of all the different cases much
     * easier.
     * </p>
     * <p>
     * For parameter and return value definition see
     * {@link ContentResolver#query(Uri, String[], String,
     * String[], String)}.
     * </p>
     *
     * @return The cursor that is returned by {@link ContentResolver#query(Uri, String[], String, String[], String)}
     *
     * @see Cursor
     * @see ContentResolver#query(Uri, String[], String, String[], String)
     */
    private Cursor query(Uri contentUri, String[] projectionItems, String selection, String[] selectionArgs, String sortOrder) {
        Cursor result = mContext.getContentResolver().query(contentUri, projectionItems, selection, selectionArgs, sortOrder);
        if (result == null)
            throw new UnsupportedOperationException("No content provider available for URI "+contentUri.toString());

        return result;
    }

    /**
     * Creates a JSON object which contains all the given parameters.
     *
     * @param id {@link Items#_ID}
     * @param name {@link Items#NAME}
     * @param price {@link Items#PRICE}
     * @param units {@link Items#UNITS}
     * @param tags {@link Items#TAGS}
     *
     * @return A JSONObject, containing all the parameters.
     *
     * @throws JSONException
     */
    protected JSONObject itemToJSONObject(long id, String name, long price, String units, String tags) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(Items._ID, id);
        json.put(Items.NAME, name);
        json.put(Items.PRICE, price);
        json.put(Items.UNITS, units);
        json.put(Items.TAGS, tags);
        return json;
    }

    /**
     * Creates a JSON Object which contains the following attributes:
     * <ul>
     * <li>{@link Items#_ID}</li>
     * <li>{@link Items#NAME}</li>
     * <li>{@link Items#PRICE}</li>
     * <li>{@link Items#UNITS}</li>
     * <li>{@link Items#TAGS}</li>
     * </ul>
     *
     * @return
     * @throws JSONException
     */
    protected JSONObject itemToJSONObject(Cursor itemCursor) throws JSONException {
        return itemToJSONObject(itemCursor.getLong(  itemCursor.getColumnIndex(Items._ID)),
                                itemCursor.getString(itemCursor.getColumnIndex(Items.NAME)),
                                itemCursor.getLong(  itemCursor.getColumnIndex(Items.PRICE)),
                                itemCursor.getString(itemCursor.getColumnIndex(Items.UNITS)),
                                itemCursor.getString(itemCursor.getColumnIndex(Items.TAGS))
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

    protected JSONObject containsToJSONObject(long item_id, String item_name, long item_price, String item_units, String item_tags, long priority, long status, String quantity) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(ContainsFull.ITEM_ID, item_id);
        json.put(ContainsFull.ITEM_NAME, item_name);
        json.put(ContainsFull.ITEM_PRICE, item_price);
        json.put(ContainsFull.ITEM_TAGS, item_tags);
        json.put(ContainsFull.ITEM_UNITS, item_units);
        json.put(ContainsFull.PRIORITY, priority);
        json.put(ContainsFull.STATUS, status);
        json.put(ContainsFull.QUANTITY, quantity);
        return json;
    }

    protected JSONObject containsToJSONObject(Cursor containsCursor) throws JSONException {
        return containsToJSONObject(containsCursor.getLong(  containsCursor.getColumnIndex(ContainsFull.ITEM_ID)),
                                    containsCursor.getString(containsCursor.getColumnIndex(ContainsFull.ITEM_NAME)),
                                    containsCursor.getLong(  containsCursor.getColumnIndex(ContainsFull.ITEM_PRICE)),
                                    containsCursor.getString(containsCursor.getColumnIndex(ContainsFull.ITEM_UNITS)),
                                    containsCursor.getString(containsCursor.getColumnIndex(ContainsFull.ITEM_TAGS)),
                                    containsCursor.getLong(  containsCursor.getColumnIndex(ContainsFull.PRIORITY)),
                                    containsCursor.getLong(  containsCursor.getColumnIndex(ContainsFull.STATUS)),
                                    containsCursor.getString(containsCursor.getColumnIndex(ContainsFull.QUANTITY))
                                    );
    }

    protected JSONArray containsToJSONArray(Cursor containsCursor) throws JSONException {
        JSONArray array = new JSONArray();

        if (containsCursor.moveToFirst())
            do {
                array.put(containsToJSONObject(containsCursor));
            } while (containsCursor.moveToNext());

        return array;
    }
}
