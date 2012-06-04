package org.openintents.wifiserver.util;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.util.Log;

public class CursorUtil {

    private static final String TAG = CursorUtil.class.getSimpleName();
    
    private CursorUtil() {}
    
    public static JSONObject convertToJSONObject(Cursor cursor) {
        JSONObject json = new JSONObject();
        
        for (int i=0; i<cursor.getColumnCount();i++) {
            String key = cursor.getColumnName(i);
            
            try {
                switch (cursor.getType(i)) {
                    case Cursor.FIELD_TYPE_FLOAT:
                        json.put(key, cursor.getFloat(i)); break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        json.put(key, cursor.getInt(i)); break;
                    case Cursor.FIELD_TYPE_STRING:
                        json.put(key, cursor.getString(i)); break;
                    case Cursor.FIELD_TYPE_BLOB:
                        json.put(key, cursor.getBlob(i)); break;
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to create JSON object!", e);
                return null;
            }
        }
        
        return json;
    }
}
