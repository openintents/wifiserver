package org.openintents.wifiserver.preference;

import org.openintents.wifiserver.util.HashUtil;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * This preference input field hashes its content before persisting it. The text
 * field will be empty every time it shows up, so it will not print the text that has been
 * typed in previously. The following hash algorithm is used:<br />
 * <ul>
 * <li>generate a random string of 8 characters</li>
 * <li>append the salt to the end of the input string</li>
 * <li>hash the salted string with SHA-256</li>
 * <li>append the salt to the end of the hash</li>
 * <li>persist this hash</li>
 * </ul>
 *
 * @author Stanley Förster
 *
 */
public class HashedEditTextPreference extends EditTextPreference {

    private final static String TAG = HashedEditTextPreference.class.getSimpleName();

    /**
     * Creates a new preference field as defined by
     * {@link EditTextPreference#EditTextPreference(Context, AttributeSet, int)}
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public HashedEditTextPreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Creates a new preference field as defined by
     * {@link EditTextPreference#EditTextPreference(Context, AttributeSet)}
     *
     * @param context
     * @param attrs
     */
    public HashedEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Creates a new preference field as defined by
     * {@link EditTextPreference#EditTextPreference(Context)}
     *
     * @param context
     */
    public HashedEditTextPreference(Context context) {
        super(context);
    }

    /**
     * Returns an empty string, because the original value is hashed before
     * saving it.
     *
     * @return An empty string.
     */
    @Override
    public String getText() {
        return "";
    }

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     * Before the value is saved, it will be salted and hashed with SHA-256
     * algorithm. The salt is then again appended to the hashed value.
     * If the value is empty, nothing will be changed.
     */
    @Override
    public void setText(String text) {
        /*
         * The check is required because Android gets all preferences and writes
         * them back when the preference activity is going to open. This
         * behavior would mess up the hashed password, so let's only change it
         * if the user wants.
         */
        if (text.equals(getPersistedString("")))
            return;

        String salt = HashUtil.generateSalt();
        String saltedPW = text.concat(salt);
        super.setText(HashUtil.sha256(saltedPW).concat(salt));
    }
}
