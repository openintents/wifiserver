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

public class GetNote extends NotesHandler {

    private static final String[] PROJECTION = new String[] {"_id", "title", "note", "tags", "created", "modified"};

    public GetNote(Context context) {
        super(context);
    }

    @Override
    public void getResponse(HttpRequest request, HttpResponse response, HttpContext context) {
        if (!"GET".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return;
        }

        String id = URLUtil.getParameter(request.getRequestLine().getUri(), "id");

        if (id == null) {
            Cursor notesCursor = mContext.getContentResolver().query(mNotesURI, PROJECTION, null, null, null);

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

    protected JSONObject noteToJSONObject(Cursor notesCursor) throws JSONException {
        return noteToJSONObject(notesCursor.getInt(notesCursor.getColumnIndex("_id")),
                        notesCursor.getString(notesCursor.getColumnIndex("title")),
                        notesCursor.getString(notesCursor.getColumnIndex("note")),
                        notesCursor.getString(notesCursor.getColumnIndex("tags")),
                        notesCursor.getLong(notesCursor.getColumnIndex("created")),
                        notesCursor.getLong(notesCursor.getColumnIndex("modified")));
    }

    protected JSONArray notesToJSONArray(Cursor notesCursor) throws JSONException {
        JSONArray array = new JSONArray();

        if (notesCursor.moveToFirst())
            do {
                array.put(noteToJSONObject(notesCursor));
            } while (notesCursor.moveToNext());

        return array;
    }
}