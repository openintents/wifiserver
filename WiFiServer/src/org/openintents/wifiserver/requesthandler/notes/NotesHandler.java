package org.openintents.wifiserver.requesthandler.notes;

import org.openintents.wifiserver.requesthandler.BasicAuthentiationHandler;

import android.content.Context;
import android.net.Uri;

public abstract class NotesHandler extends BasicAuthentiationHandler {

    protected final static String TAG = NotesHandler.class.getSimpleName();
    protected final static Uri mNotesURI = Uri.parse("content://org.openintents.notepad/notes");
    protected final Context mContext;

    public NotesHandler(Context context) {
        this.mContext = context;
    }
}
