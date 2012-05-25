package org.openintents.wifiserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.openintents.wifiserver.webserver.WebServer;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

@EActivity(R.layout.main)
@OptionsMenu(R.menu.menu)
public class OIWiFiServerActivity extends Activity {

    private final static String  TAG                       = OIWiFiServerActivity.class.getSimpleName();
    
    @ViewById protected TextView     textWifiStatus;
    @ViewById protected TextView     textURL;
    @ViewById protected TextView     textPasswordShown;
    @ViewById protected TextSwitcher textSwitcherPassword;
    @ViewById protected ToggleButton toggleStartStopServer;
    
    @Pref protected OiWiFiPreferences_ prefs;

    private ConnectivityReceiver mConnectivityReceiver     = null;
    private WebServer mWebServer = null;

    
////////////////////////////////////////////////////////////////////////////////
//////////////////////////////   LIVECYCLE   ///////////////////////////////////  
////////////////////////////////////////////////////////////////////////////////
    
    @AfterViews
    protected void onCreate() {
        mConnectivityReceiver = new ConnectivityReceiver() {
            @Override
            public void onConnectionChanged(ConnectionType type) {
                if (false && type != ConnectionType.NET_WIFI) {
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
        stopServer();
        if (mConnectivityReceiver != null)
            this.unregisterReceiver(mConnectivityReceiver);
    }
    
////////////////////////////////////////////////////////////////////////////////
/////////////////////////////   INTERACTION   //////////////////////////////////  
////////////////////////////////////////////////////////////////////////////////
    
    @Click
    protected void toggleStartStopServer() {
        if (toggleStartStopServer.isChecked())
            startServer();
        else
            stopServer();
    }
    
    @Click
    protected void textSwitcherPassword() {
        textSwitcherPassword.showNext();
    }
    
////////////////////////////////////////////////////////////////////////////////
////////////////////////////////   MENU   //////////////////////////////////////  
////////////////////////////////////////////////////////////////////////////////
    
    @OptionsItem
    protected void menuPreferences() {
        startActivity(new Intent(this, OIWiFiPreferencesActivity_.class));
    }

////////////////////////////////////////////////////////////////////////////////
///////////////////////////////   SERVER   /////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////
    
    private void startServer() {
        if (mWebServer == null) {
            if (prefs.sslEnable().get())
                try {
                    mWebServer = new WebServer(prefs.sslPort().get(), true, getAssets().open("oi.bks"), "oenintents".toCharArray());
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
            else
                mWebServer = new WebServer(prefs.port().get());
        }
        mWebServer.start();
        
        textURL.setText(getDeviceIPAddress()+":"+mWebServer.getPort()); // TODO move to callback function
    }

    private void stopServer() {
        if (mWebServer != null)
            mWebServer.stop();
        
        textURL.setText(""); //TODO move to callback function
    }
    
    private String getDeviceIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }

        return null;
    }
}