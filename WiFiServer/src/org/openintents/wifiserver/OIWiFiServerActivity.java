package org.openintents.wifiserver;

import java.io.IOException;

import org.openintents.wifiserver.preference.OIWiFiPreferencesActivity_;
import org.openintents.wifiserver.preference.OiWiFiPreferences_;
import org.openintents.wifiserver.requesthandler.FileHandler_;
import org.openintents.wifiserver.requesthandler.LoginHandler_;
import org.openintents.wifiserver.requesthandler.notes.DeleteNote;
import org.openintents.wifiserver.requesthandler.notes.GetNote;
import org.openintents.wifiserver.requesthandler.notes.NewNote;
import org.openintents.wifiserver.requesthandler.notes.UpdateNote;
import org.openintents.wifiserver.webserver.ServerStatusListener;
import org.openintents.wifiserver.webserver.WebServer;
import org.openintents.wifiserver.webserver.WebServer.Status;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

@SuppressLint("Registered")
@EActivity(R.layout.main)
@OptionsMenu(R.menu.menu)
public class OIWiFiServerActivity extends Activity {

    private final static String  TAG                       = OIWiFiServerActivity.class.getSimpleName();

    @ViewById protected TextView     textWifiStatus;
    @ViewById protected TextView     textURL;
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
                if (type != ConnectionType.NET_WIFI)
                    wifiDisconnected();
                else
                    wifiConnected();
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

    private void wifiDisconnected() {
        textWifiStatus.setText(R.string.disconnected);
        if (toggleStartStopServer.isChecked())
            toggleStartStopServer.toggle();
        toggleStartStopServer.setEnabled(false);
        stopServer();
    }

    private void wifiConnected() {
        textWifiStatus.setText(R.string.connected);
        toggleStartStopServer.setEnabled(true);
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
                    mWebServer = new WebServer(prefs.sslPort().get(), true, getAssets().open("oi.bks"), "openintents".toCharArray());
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
            else
                mWebServer = new WebServer(prefs.port().get());

            if (prefs.passwordEnable().get()) {
                    mWebServer.addRequestInterceptor(new AuthenticationInterceptor(prefs.customPassword().get()));
            }

            mWebServer.registerRequestHandler("*",              FileHandler_.getInstance_(this));
            mWebServer.registerRequestHandler("/notes/get*",    new GetNote(this));
            mWebServer.registerRequestHandler("/notes/delete*", new DeleteNote(this));
            mWebServer.registerRequestHandler("/notes/new",     new NewNote(this));
            mWebServer.registerRequestHandler("/notes/update",  new UpdateNote(this));
            mWebServer.registerRequestHandler("/login",         LoginHandler_.getInstance_(this));

            mWebServer.addListener(new ServerStatusListener() {
                @Override
                public void onStatusChanged(Status status, String msg) {
                    switch (status) {
                        case ERROR: Toast.makeText(OIWiFiServerActivity.this, "Server Error: "+msg, Toast.LENGTH_LONG).show(); break;
                        case STARTED: serverStarted(); break;
                        case STOPPED: serverStopped();
                    }
                }
            });
        }

        mWebServer.start();
    }

    private void stopServer() {
        if (mWebServer != null)
            mWebServer.stop();
        mWebServer = null;
    }

    private void serverStopped() {
        toggleStartStopServer.setChecked(false);
        textURL.setText("");
    }

    private void serverStarted() {
        textURL.setText(buildURLString());
    }


////////////////////////////////////////////////////////////////////////////////
////////////////////////////////   UTIL   //////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    private String buildURLString() {
        return (prefs.sslEnable().get() ? "https" : "http") + "://" + getDeviceIPAddress()+":"+mWebServer.getPort();
    }

    private String getDeviceIPAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }
}
