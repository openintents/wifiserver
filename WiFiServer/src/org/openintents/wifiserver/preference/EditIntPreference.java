package org.openintents.wifiserver.preference;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Special preference text input field that stores its content as integer.
 * This is necessary to use type safe preferences of Android Annotations. 
 * 
 * @author Stanley Förster
 * @author <a href="http://stackoverflow.com/a/3755608/579698">Brutall</a>
 *
 */
public class EditIntPreference extends EditTextPreference {
    public EditIntPreference(Context context) {
        super(context);
    }

    public EditIntPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditIntPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return String.valueOf(getPersistedInt(-1));
    }

    @Override
    protected boolean persistString(String value) {
        return persistInt(Integer.parseInt(value));
    }
}
