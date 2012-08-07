package org.openintents.wifiserver.requesthandler.shoppinglist;

import org.openintents.wifiserver.requesthandler.BasicAuthentiationHandler;
import org.openintents.wifiserver.requesthandler.notes.NotesHandler;

import android.content.Context;

public abstract class ShoppinglistHandler extends BasicAuthentiationHandler {
    protected final static String TAG = NotesHandler.class.getSimpleName();
    protected final Context mContext;

    public ShoppinglistHandler(Context context) {
        this.mContext = context;
    }
}
