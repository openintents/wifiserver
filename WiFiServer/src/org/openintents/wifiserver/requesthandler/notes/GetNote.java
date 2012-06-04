package org.openintents.wifiserver.requesthandler.notes;

import java.io.UnsupportedEncodingException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;
import org.openintents.wifiserver.util.CursorUtil;
import org.openintents.wifiserver.util.URLUtil;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class GetNote extends NotesHandler {
    
    public GetNote(Context context) {
        super(context);
    }

    @Override
    public void getResponse(HttpRequest request, HttpResponse response, HttpContext context) {
        if (!"GET".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return ;
        }
        
        String id = URLUtil.getParameter(request.getRequestLine().getUri(), "id");
        
        if (id == null) {
            response.setStatusCode(400);
        } else {
            Cursor notesCursor = mContext.getContentResolver().query(mNotesURI, null, "_id = ?", new String[] { id }, null);
            if (!notesCursor.moveToFirst()) {
                response.setStatusCode(404);
                notesCursor.close();
                return;
            }

            JSONObject json = CursorUtil.convertToJSONObject(notesCursor);
            
            if (json == null) {
                response.setStatusCode(500);
                return;
            }
                
            try {
                AbstractHttpEntity entity = new StringEntity(json.toString());
                entity.setContentType("application/json");
                response.setEntity(entity);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Failed to convert JSON object to string!", e);
                response.setStatusCode(500);
            }
        }
    }
}