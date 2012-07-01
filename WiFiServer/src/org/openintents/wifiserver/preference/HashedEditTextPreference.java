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

    public HashedEditTextPreference(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    public HashedEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HashedEditTextPreference(Context context) {
        super(context);
    }

    @Override
    public String getText() {
        return "";
    }

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
