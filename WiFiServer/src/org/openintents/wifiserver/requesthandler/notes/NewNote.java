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

/**
 * Handler which is used to create new notes. It handles requests of the form "/notes/new".
 *
 * @author Stanley Förster
 *
 */
public class NewNote extends NotesHandler {

    /**
     * Creates a new handler.
     *
     * @param context The application's context.
     */
    public NewNote(Context context) {
        super(context);
    }

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     *
     * This handler creates a new note with the given attributes. The request
     * method must be POST, otherwise a status code 405 will be returned.
     * At least a <code>title</code> and the <code>note</code> itself are
     * required, <code>tags</code> is a optional field. If these parameters are
     * not available, status code 400 will be returned. If the parameter parsing
     * failed, 500 will be returned.
     * In all other cases a new note will be created.
     */
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
            String tags = null;

            List<NameValuePair> postParams;
            try {
                postParams = URLEncodedUtils.parse(postEntity);
            } catch (IOException e) {
                Log.e(TAG, "Failed to parse parameters!", e);
                response.setStatusCode(500);
                return;
            }

            for (NameValuePair nvp : postParams) {
                if ("note".equals(nvp.getName()))
                    note = nvp.getValue();
                if ("title".equals(nvp.getName()))
                    title = nvp.getValue();
                if ("tags".equals(nvp.getName()))
                    tags = nvp.getValue();
            }

            if (title == null || note == null) {
                response.setStatusCode(400);
                return;
            }

            ContentValues values = new ContentValues();
            values.put("modified", Long.valueOf(System.currentTimeMillis()));
            values.put("created", Long.valueOf(System.currentTimeMillis()));
            values.put("title", title);
            values.put("note", note);
            if (tags != null) {
                values.put("tags", tags);
            }
            // prevent notepad app from throwing IndexOutOfBoundsException
            values.put("selection_start", Long.valueOf(0));
            values.put("selection_end", Long.valueOf(0));

            mContext.getContentResolver().insert(mNotesURI, values);
        }
    }
}
