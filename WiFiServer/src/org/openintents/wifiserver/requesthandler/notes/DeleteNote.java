package org.openintents.wifiserver.requesthandler.notes;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.openintents.wifiserver.util.URLUtil;

import android.content.Context;

public class DeleteNote extends NotesHandler {

    public DeleteNote(Context context) {
        super(context);
    }

    @Override
    protected void getResponse(HttpRequest request, HttpResponse response, HttpContext context) {
        if (!"GET".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return;
        }

        String id = URLUtil.getParameter(request.getRequestLine().getUri(), "id");

        if (id == null) {
            mContext.getContentResolver().delete(mNotesURI, null, null);
        } else {
            mContext.getContentResolver().delete(mNotesURI, "_id = ?", new String[] { id });
        }
    }
}
