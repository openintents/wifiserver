package org.openintents.wifiserver.requesthandler.shoppinglist;

import static android.provider.BaseColumns._ID;
import static org.openintents.shopping.library.provider.ShoppingContract.Lists.CONTENT_URI;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.openintents.shopping.library.util.ShoppingUtils;
import org.openintents.wifiserver.util.URLUtil;

import android.content.Context;
import android.database.Cursor;

/**
 * Handler which is used to delete shopping lists. It handles requests of the form "/shoppinglist/list/delete".
 *
 * @author Stanley Förster
 *
 */
public class DeleteShoppinglist extends ShoppinglistHandler {

    /**
     * Creates a new handler.
     *
     * @param context The application's context.
     */
    public DeleteShoppinglist(Context context) {
        super(context);
    }

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     *
     * This method deletes a shopping list. The required HTTP method is GET.
     * Every other method will cause a 405 status code to be returned.
     * If no parameter is given, all lists will be deleted.
     * To specify a list, the <code>id</code> parameter can be used:
     * <ul>
     * <li><code>id</code> specifies the item, that should be deleted. It will be removed from all lists and from the app's database.</li>
     * </ul>
     *
     * @see ShoppingUtils#deleteList(Context, String)
     */
    @Override
    public void getResponse(HttpRequest request, HttpResponse response, HttpContext context) {
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

    /**
     * Deletes the shopping list with the given id.
     *
     * @param id The id of the list, that should be deleted.
     *
     * @see ShoppingUtils#deleteList(Context, String)
     */
    private void deleteList(String id) {
        ShoppingUtils.deleteList(mContext, id);
    }
}
