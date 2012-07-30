package org.openintents.wifiserver.requesthandler.shoppinglist;

import org.apache.http.protocol.HttpRequestHandler;
import org.openintents.wifiserver.requesthandler.notes.NotesHandler;

import android.content.Context;

public abstract class ShoppinglistHandler implements HttpRequestHandler {
    protected final static String TAG = NotesHandler.class.getSimpleName();
    protected final Context mContext;

    public ShoppinglistHandler(Context context) {
        this.mContext = context;
    }
}
