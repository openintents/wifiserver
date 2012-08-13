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

    /**
     * Creates a new preference field as defined by
     * {@link EditTextPreference#EditTextPreference(Context)}
     *
     * @param context
     */
    public EditIntPreference(Context context) {
        super(context);
    }

    /**
     * Creates a new preference field as defined by
     * {@link EditTextPreference#EditTextPreference(Context, AttributeSet)}
     *
     * @param context
     * @param attrs
     */
    public EditIntPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Creates a new preference field as defined by
     * {@link EditTextPreference#EditTextPreference(Context, AttributeSet, int)}
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public EditIntPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     * The default value is -1.
     */
    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return String.valueOf(getPersistedInt(-1));
    }

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     * Persists the given string as an integer by using
     * {@link Integer#parseInt(String)}
     */
    @Override
    protected boolean persistString(String value) {
        return persistInt(Integer.parseInt(value));
    }
}
