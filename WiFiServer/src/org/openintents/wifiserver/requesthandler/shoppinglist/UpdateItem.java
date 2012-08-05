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
import org.openintents.shopping.library.util.ShoppingUtils;
import org.openintents.wifiserver.util.URLEncodedUtils;

import android.content.Context;
import android.util.Log;

public class UpdateItem extends ShoppinglistHandler {

    public UpdateItem(Context context) {
        super(context);
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (!"POST".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return;
        }

        Log.d(TAG, "Update/Creat item");

        if (request instanceof BasicHttpEntityEnclosingRequest) {
            HttpEntity postEntity = ((BasicHttpEntityEnclosingRequest)request).getEntity();

            String name = null;
            String tags = null;
            String price = null;

            List<NameValuePair> postParams;
            try {
                postParams = URLEncodedUtils.parse(postEntity);
            } catch (IOException e) {
                Log.e(TAG, "Failed to parse parameters!", e);
                response.setStatusCode(500);
                return;
            }

            for (NameValuePair nvp : postParams) {
                if (ShoppingContract.Items.NAME.equals(nvp.getName()))
                    name = nvp.getValue();
                if (ShoppingContract.Items.TAGS.equals(nvp.getName()))
                    tags = nvp.getValue();
                if (ShoppingContract.Items.PRICE.equals(nvp.getName()))
                    price = nvp.getValue();
            }

            Log.d(TAG, "Item: "+name);

            if (name == null || name.equals("")) {
                response.setStatusCode(400);
                return;
            }

            ShoppingUtils.updateOrCreateItem(mContext, name, tags, price, null);
        }
    }
}
