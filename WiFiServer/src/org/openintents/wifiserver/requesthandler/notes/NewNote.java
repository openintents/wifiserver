package org.openintents.wifiserver.requesthandler.notes;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HttpContext;
import org.openintents.wifiserver.util.URLEncodedUtils;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

public class NewNote extends NotesHandler {

    public NewNote(Context context) {
        super(context);
    }

    @Override
    protected void getResponse(HttpRequest request, HttpResponse response, HttpContext context) {
        if (!"POST".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return;
        }

        if (request instanceof BasicHttpEntityEnclosingRequest) {
            HttpEntity postEntity = ((BasicHttpEntityEnclosingRequest)request).getEntity();

            String title = null;
            String note = null;

            List<NameValuePair> postParams;
            try {
                postParams = URLEncodedUtils.parse(postEntity);
            } catch (IOException e) {
                Log.e(TAG, "Failed to parse parameters!", e);
                response.setStatusCode(500);
                return;
            }

            for (NameValuePair nvp : postParams) {
                Log.d(TAG, "("+nvp.getName()+"|"+nvp.getValue()+")");
                if ("text".equals(nvp.getName()))
                    note = nvp.getValue();
                if ("title".equals(nvp.getName()))
                    title = nvp.getValue();
            }

            if (title == null || note == null) {
                response.setStatusCode(400);
            }

            ContentValues values = new ContentValues();
            values.put("modified", new Long(System.currentTimeMillis()));
            values.put("created", new Long(System.currentTimeMillis()));
            values.put("title", title);
            values.put("note", note);

            // prevent notepad app from throwing IndexOutOfBoundsException
            values.put("selection_start", new Long(0));
            values.put("selection_end", new Long(0));

            mContext.getContentResolver().insert(mNotesURI, values);
        }
    }

}
