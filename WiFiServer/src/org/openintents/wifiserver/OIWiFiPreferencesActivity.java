package org.openintents.wifiserver;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class OIWiFiPreferencesActivity extends PreferenceActivity {

    /* (non-Javadoc)
     * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
