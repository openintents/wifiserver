package org.openintents.wifiserver.requesthandler.notes;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.openintents.wifiserver.util.URLUtil;

import android.content.Context;

/**
 * Handler which is used to delete notes. It handles requests of the form "/notes/delete".
 *
 * @author Stanley Förster
 *
 */
public class DeleteNote extends NotesHandler {

    /**
     * Creates a new handler.
     *
     * @param context The application's context.
     */
    public DeleteNote(Context context) {
        super(context);
    }

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     * The request first parses the URL for an "id" parameter. If it is present,
     * the id is used to delete a specific note. In all other cases, all notes
     * will be deleted. This handler always returns a status code 200.
     */
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
