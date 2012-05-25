package org.openintents.wifiserver;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.main)
public class OIWiFiServerActivity extends Activity {

    private final static String  TAG                       = OIWiFiServerActivity.class.getSimpleName();
    
    @ViewById protected TextView     textWifiStatus;
    @ViewById protected TextView     textURL;
    @ViewById protected TextView     textPasswordShown;
    @ViewById protected TextSwitcher textSwitcherPassword;
    @ViewById protected ToggleButton toggleStartStopServer;
    
    private ConnectivityReceiver mConnectivityReceiver     = null;

    
////////////////////////////////////////////////////////////////////////////////
//////////////////////////////   LIVECYCLE   ///////////////////////////////////  
////////////////////////////////////////////////////////////////////////////////
    
    @AfterViews
    protected void onCreate() {
        textSwitcherPassword.setEnabled(false);
        mConnectivityReceiver = new ConnectivityReceiver() {
            @Override
            public void onConnectionChanged(ConnectionType type) {
                if (type != ConnectionType.NET_WIFI) {
                    textWifiStatus.setText(R.string.disconnected);
                    if (toggleStartStopServer.isChecked())
                        toggleStartStopServer.toggle();
                    toggleStartStopServer.setEnabled(false);
                } else {
                    textWifiStatus.setText(R.string.connected);
                    toggleStartStopServer.setEnabled(true);
                }
            }
        };
        this.registerReceiver(mConnectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnectivityReceiver != null)
            this.unregisterReceiver(mConnectivityReceiver);
    }
}