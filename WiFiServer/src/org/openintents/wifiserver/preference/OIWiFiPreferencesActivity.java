package org.openintents.wifiserver.preference;

import org.openintents.wifiserver.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.res.StringRes;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

@EActivity
public class OIWiFiPreferencesActivity extends PreferenceActivity implements OnPreferenceChangeListener {
    
    @StringRes protected String prefsSSLPortKey;
    @StringRes protected String prefsPortKey;
    
    @StringRes protected String errorPortBoundaries;
    @StringRes protected String errorPortDuplicate;

    @Pref protected OiWiFiPreferences_ prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().findPreference(prefsSSLPortKey).setOnPreferenceChangeListener(this);
        getPreferenceScreen().findPreference(prefsPortKey).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int port = Integer.parseInt(newValue.toString());

        if (port < 1000 || port > 65535) {
            showToast(errorPortBoundaries);
            return false;
        }
        
        int otherPort = -1;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); 
                        
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
    
    private void showToast(String msg) {
        Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }
}
