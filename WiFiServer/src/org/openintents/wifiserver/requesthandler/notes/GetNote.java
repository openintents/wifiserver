package org.openintents.wifiserver.requesthandler.notes;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openintents.wifiserver.util.URLUtil;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * Handler which is used to retrieve notes. It handles requests of the form "/notes/get".
 *
 * @author Stanley Förster
 *
 */
public class GetNote extends NotesHandler {

    private static final String[] PROJECTION = new String[] {"_id", "title", "note", "tags", "created", "modified"};

    /**
     * Creates a new handler.
     *
     * @param context The application's context.
     */
    public GetNote(Context context) {
        super(context);
    }

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     *
     * This method handles requests to retrieve notes and returns them as JSON
     * representation.
     * Only the GET method is accepted, everything else, causes a status code
     * 405 to be returned.
     * The URL is parsed for an <code>id</code> parameter. If it is present, the
     * note with that id will be returned or a status code 404, if no note with
     * this id is available. If no id if given, a list of all notes will be
     * returned.
     * If the notepad app is not available on the device, a status code 501 is
     * returned.
     */
    @Override
    public void getResponse(HttpRequest request, HttpResponse response, HttpContext context) {
        if (!"GET".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return;
        }

        String id = URLUtil.getParameter(request.getRequestLine().getUri(), "id");

        if (id == null) {
            Cursor notesCursor = mContext.getContentResolver().query(mNotesURI, PROJECTION, null, null, null);

            if (notesCursor == null) {
                response.setStatusCode(501);
                return;
            }

            try {
                AbstractHttpEntity entity = new StringEntity(notesToJSONArray(notesCursor).toString());
                entity.setContentType("application/json");
                response.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Failed to create entity!", e);
                response.setStatusCode(500);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create JSON Array", e);
                response.setStatusCode(500);
            }

            notesCursor.close();
        } else {
            Cursor notesCursor = mContext.getContentResolver().query(mNotesURI, PROJECTION, "_id = ?", new String[] { id }, null);

            if (notesCursor == null) {
                response.setStatusCode(501);
                return;
            }

            if (!notesCursor.moveToFirst()) {
                response.setStatusCode(404);
                notesCursor.close();
                return;
            }

            try {
                AbstractHttpEntity entity = new StringEntity(noteToJSONObject(notesCursor).toString());
                entity.setContentType("application/json");
                response.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Failed to create entity!", e);
                response.setStatusCode(500);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create JSON Object", e);
                response.setStatusCode(500);
            }
        }
    }

    /**
     * This method creates a new JSONObject, containing all the parameters.
     *
     * @param id
     * @param title
     * @param note
     * @param tags
     * @param createdDate
     * @param modifiedDate
     * @return A JSONObject, containing all the parameters.
     *
     * @throws JSONException
     */
    protected JSONObject noteToJSONObject(int id, String title, String note, String tags, long createdDate, long modifiedDate) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("_id", id);
        json.put("title", title);
        json.put("note", note);
        json.put("tags", tags);
        json.put("created", createdDate);
        json.put("modified", modifiedDate);

        return json;
    }

    /**
     * This method converts a single line of a cursor into a {@link JSONObject}.
     * The cursor has to point to the row, which should be converted.
     * The cursor has to contain the following columns:
     * <ul>
     * <li>_id
     * <li>
     * <li>title</li>
     * <li>note</li>
     * <li>tags</li>
     * <li>created</li>
     * <li>modified</li>
     * </ul>
     *
     * @param notesCursor
     *            A cursor, whose current row should be converted into a
     *            JSONObject.
     * @return The JSONObject, that represents the note.
     *
     * @throws JSONException
     */
    protected JSONObject noteToJSONObject(Cursor notesCursor) throws JSONException {
        return noteToJSONObject(notesCursor.getInt(notesCursor.getColumnIndex("_id")),
                        notesCursor.getString(notesCursor.getColumnIndex("title")),
                        notesCursor.getString(notesCursor.getColumnIndex("note")),
                        notesCursor.getString(notesCursor.getColumnIndex("tags")),
                        notesCursor.getLong(notesCursor.getColumnIndex("created")),
                        notesCursor.getLong(notesCursor.getColumnIndex("modified")));
    }

    /**
     * This method is used to convert all rows of a cursor into a
     * {@link JSONArray}.
     * If the cursor is empty, an empty array will be returned. Otherwise every
     * row of the cursor will be converted into a {@link JSONObject}, which will
     * be appended to the array.
     *
     * @param notesCursor
     *            A cursor, containing notes.
     * @return A JSONArray of JSONObject, which represent all the notes of the
     *         cursor.
     *
     * @throws JSONException
     */
    protected JSONArray notesToJSONArray(Cursor notesCursor) throws JSONException {
        JSONArray array = new JSONArray();

        if (notesCursor.moveToFirst())
            do {
                array.put(noteToJSONObject(notesCursor));
            } while (notesCursor.moveToNext());

        return array;
    }
}