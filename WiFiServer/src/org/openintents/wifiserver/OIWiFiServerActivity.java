package org.openintents.wifiserver;

import java.io.IOException;

import org.openintents.distribution.DistributionLibraryActivity;
import org.openintents.wifiserver.preference.OIWiFiPreferencesActivity_;
import org.openintents.wifiserver.preference.OiWiFiPreferences_;
import org.openintents.wifiserver.requesthandler.FileHandler_;
import org.openintents.wifiserver.requesthandler.LoginHandler_;
import org.openintents.wifiserver.requesthandler.LogoutHandler;
import org.openintents.wifiserver.requesthandler.notes.DeleteNote;
import org.openintents.wifiserver.requesthandler.notes.GetNote;
import org.openintents.wifiserver.requesthandler.notes.NewNote;
import org.openintents.wifiserver.requesthandler.notes.UpdateNote;
import org.openintents.wifiserver.requesthandler.shoppinglist.DeleteItem;
import org.openintents.wifiserver.requesthandler.shoppinglist.DeleteShoppinglist;
import org.openintents.wifiserver.requesthandler.shoppinglist.GetItem;
import org.openintents.wifiserver.requesthandler.shoppinglist.GetShoppinglist;
import org.openintents.wifiserver.requesthandler.shoppinglist.NewShoppinglist;
import org.openintents.wifiserver.requesthandler.shoppinglist.RenameShoppinglist;
import org.openintents.wifiserver.requesthandler.shoppinglist.UpdateItem;
import org.openintents.wifiserver.webserver.ServerStatusListener;
import org.openintents.wifiserver.webserver.WebServer;
import org.openintents.wifiserver.webserver.WebServer.Status;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.Menu;
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

/**
 * This class represents the main activity. It cannot be used directly by the
 * manifest file, becuase it is subclassed by AndroidAnnotations. Instead use
 * OIWifiServerActivity_ when referencing this class.
 *
 * @author Stanley Förster
 */
@SuppressLint("Registered")
@EActivity(R.layout.main)
@OptionsMenu(R.menu.menu)
public class OIWiFiServerActivity extends DistributionLibraryActivity {

    private static final int           MENU_DISTRIBUTION_START   = Menu.FIRST + 100;      // MUST BE LAST
    private static final int           DIALOG_DISTRIBUTION_START = 100;                   // MUST BE LAST

    private final static String        TAG                       = OIWiFiServerActivity.class.getSimpleName();

    @ViewById protected TextView       textWifiStatus;
    @ViewById protected TextView       textURL;
    @ViewById protected ToggleButton   toggleStartStopServer;

    /**
     * Represents shared preferences of the application. Use this field to
     * access and modify preferences.
     */
    @Pref protected OiWiFiPreferences_ prefs;

    private ConnectivityReceiver       mConnectivityReceiver     = null;
    private WebServer                  mWebServer                = null;


////////////////////////////////////////////////////////////////////////////////
//////////////////////////////   LIVECYCLE   ///////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * This method is called after AndroidAnnotations injected the views in the
     * {@link #onCreate(android.os.Bundle)} method.<br />
     * Use this one instead of the original {@link #onCreate(android.os.Bundle)}
     * method to avoid exceptions because of uninitialized fields.
     */
    @AfterViews
    protected void onCreate() {
        mDistribution.setFirst(MENU_DISTRIBUTION_START, DIALOG_DISTRIBUTION_START);
        if (mDistribution.showEulaOrNewVersion())
            return;

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

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     * If the server is running, it will be stopped and the connectivity
     * receiver will be unregistered.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopServer();
        if (mConnectivityReceiver != null)
            this.unregisterReceiver(mConnectivityReceiver);
    }

    /**
     * After the connectivity receiver received a connection changed event which
     * indicated that the WiFi connection is lost, this method is called, which
     * stops the server and disables the start server button.
     */
    private void wifiDisconnected() {
        textWifiStatus.setText(R.string.disconnected);
        if (toggleStartStopServer.isChecked())
            toggleStartStopServer.toggle();
        toggleStartStopServer.setEnabled(false);
        stopServer();
    }

    /**
     * After a connection event which indicates that a WiFi connection has been
     * established, this method is called and updates the GUI.
     */
    private void wifiConnected() {
        textWifiStatus.setText(R.string.connected);
        toggleStartStopServer.setEnabled(true);
    }
////////////////////////////////////////////////////////////////////////////////
/////////////////////////////   INTERACTION   //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

    /**
     * This methods represents an onClickListener of the start/stop server
     * toggle button.
     */
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

    /**
     * This is a representation of an onClickListener of the preferences menu
     * button, which starts the preferences activity.
     */
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
                    mWebServer.addRequestInterceptor(new AuthenticationInterceptor());
            }

            mWebServer.registerRequestHandler("*",              FileHandler_.getInstance_(this));
            mWebServer.registerRequestHandler("/notes/get*",    new GetNote(this));
            mWebServer.registerRequestHandler("/notes/delete*", new DeleteNote(this));
            mWebServer.registerRequestHandler("/notes/new",     new NewNote(this));
            mWebServer.registerRequestHandler("/notes/update",  new UpdateNote(this));
            mWebServer.registerRequestHandler("/login",         LoginHandler_.getInstance_(this));
            mWebServer.registerRequestHandler("/logout",        new LogoutHandler());
            mWebServer.registerRequestHandler("/shoppinglist/list/get*",    new GetShoppinglist(this));
            mWebServer.registerRequestHandler("/shoppinglist/list/delete*", new DeleteShoppinglist(this));
            mWebServer.registerRequestHandler("/shoppinglist/list/new*",    new NewShoppinglist(this));
            mWebServer.registerRequestHandler("/shoppinglist/list/rename*", new RenameShoppinglist(this));
            mWebServer.registerRequestHandler("/shoppinglist/item/get*",    new GetItem(this));
            mWebServer.registerRequestHandler("/shoppinglist/item/update",  new UpdateItem(this));
            mWebServer.registerRequestHandler("/shoppinglist/item/delete*", new DeleteItem(this));

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

    /**
     * Stops the server if it is running.
     */
    private void stopServer() {
        if (mWebServer != null)
            mWebServer.stop();
        mWebServer = null;
    }

    /**
     * After the server has been stopped, this methid is called and updates the
     * GUI. The server URL is removed and the start/stop toggle button is
     * "un-toggled".
     */
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

    /**
     * Builds an string which represents the address that can be used with a web
     * browser to access the web server.
     * The URL has the format <code>http[s]://<i>ip</i>:<i>port</i></code>.
     *
     * @return The URL which can be used to access the web server.
     */
    private String buildURLString() {
        return (prefs.sslEnable().get() ? "https" : "http") + "://" + getDeviceIPAddress()+":"+mWebServer.getPort();
    }

    /**
     * Returns the IPv4 address of the device, which will only work when it is
     * connected via WiFi. Otherwise "0.0.0.0" will be returned.
     *
     * @return A string representation of the device's IPv4 address.
     */
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

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     * This method must be overridden to avoid compatibility issues with older
     * Android versions.
     */
    @Override
    public void onBackPressed() {
        this.finish();
    }
}
