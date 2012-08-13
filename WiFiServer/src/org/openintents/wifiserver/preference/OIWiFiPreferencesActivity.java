package org.openintents.wifiserver.preference;

import org.openintents.wifiserver.R;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.Gravity;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.res.StringRes;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

/**
 * This class represents the preferences activity. It must not be used directly,
 * because it is sub-classed by AndroidAnnotations. Instead use
 * <b>OiWiFiPreferencesActivity_</b> when referencing this class.
 *
 * @author Stanley Förster
 *
 */
@EActivity
public class OIWiFiPreferencesActivity extends PreferenceActivity implements OnPreferenceChangeListener {

    @StringRes protected String prefsSSLPortKey;
    @StringRes protected String prefsPortKey;
    @StringRes protected String prefsCustomPasswordKey;
    @StringRes protected String prefsPasswordEnableKey;

    @StringRes protected String errorPortBoundaries;
    @StringRes protected String errorPortDuplicate;
    @StringRes protected String errorPasswordNotEmpty;
    @StringRes protected String warningSetPassword;

    @Pref protected OiWiFiPreferences_ prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().findPreference(prefsSSLPortKey).setOnPreferenceChangeListener(this);
        getPreferenceScreen().findPreference(prefsPortKey).setOnPreferenceChangeListener(this);
        getPreferenceScreen().findPreference(prefsCustomPasswordKey).setOnPreferenceChangeListener(this);
        getPreferenceScreen().findPreference(prefsPasswordEnableKey).setOnPreferenceChangeListener(this);
    }

    /**
     * This callback method verifies the user's inputs like port settings and
     * the password.
     * If a validation failed the input will not be saved.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(prefsSSLPortKey) || preference.getKey().equals(prefsPortKey))
            return validatePorts(preference, newValue);

        if (preference.getKey().equals(prefsCustomPasswordKey)) {
            if (!validatePassword(preference, newValue))
                return false;
        }

        if (preference.getKey().equals(prefsPasswordEnableKey) && Boolean.valueOf(newValue.toString())) {
            showToast(warningSetPassword);
        }

        return true;
    }

    /**
     * Validates the password, which must not be empty.
     *
     * @param preference
     *            The new password preference object.
     * @param newValue
     *            The new password
     * @return <b>true</b> if password is not empty, <b>false</b> otherwise.
     */
    private boolean validatePassword(Preference preference, Object newValue) {
        if (newValue.toString().equals("")) {
            showToast(errorPasswordNotEmpty);
            return false;
        }

        return true;
    }

    /**
     * Validates the port setting. The port must be between 1000 and 65535. Ports for SSL and non-SSL connections must not be the same.
     *
     * @param preference The port preference object.
     * @param newValue The new port.
     * @return <b>true</b> if validation was successful, <b>false</b> otherwise.
     */
    private boolean validatePorts(Preference preference, Object newValue) {
        int port = Integer.parseInt(newValue.toString());

        if (port < 1000 || port > 65535) {
            showToast(errorPortBoundaries);
            return false;
        }

        int otherPort = -1;

        if (preference.getKey().equals(prefsPortKey)) {
            otherPort = prefs.sslPort().get();
        } else if (preference.getKey().equals(prefsSSLPortKey)) {
            otherPort = prefs.port().get();
        }

        if (port == otherPort) {
            showToast(errorPortDuplicate);
            return false;
        }

        return true;
    }

    /**
     * Shows a toast with the given message, the duration
     * {@link Toast#LENGTH_SHORT} and gravity {@link Gravity#CENTER_VERTICAL}
     *
     * @param msg
     *            The message to show.
     */
    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}
