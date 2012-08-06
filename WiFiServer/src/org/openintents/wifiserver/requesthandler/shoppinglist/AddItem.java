package org.openintents.wifiserver.requesthandler.shoppinglist;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpContext;
import org.openintents.shopping.library.provider.ShoppingContract;
import org.openintents.shopping.library.provider.ShoppingContract.Status;
import org.openintents.shopping.library.util.ShoppingUtils;
import org.openintents.wifiserver.util.URLEncodedUtils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

public class AddItem extends ShoppinglistHandler {

    public AddItem(Context context) {
        super(context);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (!"POST".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return;
        }

        if (request instanceof BasicHttpEntityEnclosingRequest) {
            HttpEntity postEntity = ((BasicHttpEntityEnclosingRequest)request).getEntity();

            String item_name = null;
            String item_id = null;
            String item_tags = null;
            String item_price = null;
            String item_units = null;
            String list_id = null;
            String priority = null;
            String quantity = null;
            String status = null;

            List<NameValuePair> postParams;
            try {
                postParams = URLEncodedUtils.parse(postEntity);
            } catch (IOException e) {
                Log.e(TAG, "Failed to parse parameters!", e);
                response.setStatusCode(500);
                return;
            }

            for (NameValuePair nvp : postParams) {
                if (ShoppingContract.ContainsFull.ITEM_NAME.equals(nvp.getName()))
                    item_name = nvp.getValue();
                if (ShoppingContract.ContainsFull.ITEM_ID.equals(nvp.getName()))
                    item_id = nvp.getValue();
                if (ShoppingContract.ContainsFull.ITEM_TAGS.equals(nvp.getName()))
                    item_tags = nvp.getValue();
                if (ShoppingContract.ContainsFull.ITEM_PRICE.equals(nvp.getName()))
                    item_price = nvp.getValue();
                if (ShoppingContract.ContainsFull.ITEM_UNITS.equals(nvp.getName()))
                    item_units = nvp.getValue();
                if (ShoppingContract.ContainsFull.LIST_ID.equals(nvp.getName()))
                    list_id = nvp.getValue();
                if (ShoppingContract.ContainsFull.PRIORITY.equals(nvp.getName()))
                    priority = nvp.getValue();
                if (ShoppingContract.ContainsFull.QUANTITY.equals(nvp.getName()))
                    quantity = nvp.getValue();
                if (ShoppingContract.ContainsFull.STATUS.equals(nvp.getName()))
                    status = nvp.getValue();
            }

            // if no item is specified --> bad request
            if ((item_name == null || item_name.equals("")) && (item_id == null || item_id.equals(""))) {
                response.setStatusCode(400);
                return;
            }

            // if item id is not given --> create new item or get id from database and update item's attributes
            if (item_id == null || item_id.equals("")) {
                // update or create in items table
                item_id = "" + ShoppingUtils.updateOrCreateItem(mContext, item_name, item_tags, item_price, null);
            } else {
                // update item
                ContentValues cv = new ContentValues();
                if (item_name != null)
                    cv.put(ShoppingContract.Items.NAME, item_name);
                if (item_tags != null)
                    cv.put(ShoppingContract.Items.TAGS, item_tags);
                if (item_price != null)
                    cv.put(ShoppingContract.Items.PRICE, Double.valueOf(100*Double.valueOf(item_price)).longValue());

                mContext.getContentResolver().update(ShoppingContract.Items.CONTENT_URI, cv, ShoppingContract.Items._ID + " = ?", new String[] {item_id});
            }

            // if unit is given --> update
            if (item_units != null) {
                ContentValues cv = new ContentValues();
                cv = new ContentValues();
                cv.put(ShoppingContract.Items.UNITS, item_units);
                mContext.getContentResolver().update(ShoppingContract.Items.CONTENT_URI, cv, ShoppingContract.Items._ID + " = ?", new String[] {item_id} );
            }

            if (list_id == null || list_id.equals("")) {
                return;
            }

            if (status == null || status.equals(""))
                status = "" + Status.WANT_TO_BUY;

            ShoppingUtils.addItemToList(mContext, Long.valueOf(item_id), Long.valueOf(list_id), Long.valueOf(status), priority, quantity, false, false, false);
        }
    }
}
