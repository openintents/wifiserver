package org.openintents.wifiserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * The connectivity receiver is a specialized {@link BroadcastReceiver} which is
 * used to notice if a WiFi connection has been established or lost.
 *
 * @author Stanley Förster
 *
 */
public abstract class ConnectivityReceiver extends BroadcastReceiver {

    /**
     * Types of
     *
     * @author Stanley Förster
     *
     */
    enum ConnectionType {
        NET_3G,
        NET_WIFI,
        NET_NONE
    }

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     * After connection state changed the
     * {@link #onConnectionChanged(ConnectionType)} method is invoked to notify
     * about the changed connection.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                if (info.getType() == ConnectivityManager.TYPE_MOBILE)
                    onConnectionChanged(ConnectionType.NET_3G);
                else if (info.getType() == ConnectivityManager.TYPE_WIFI)
                    onConnectionChanged(ConnectionType.NET_WIFI);
                else
                    onConnectionChanged(ConnectionType.NET_NONE);
            }

            if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false))
                onConnectionChanged(ConnectionType.NET_NONE);
        }
    }

    /**
     * This method is invoked after the connection changed.
     *
     * @param type
     *            the new type of connection.
     */
    public abstract void onConnectionChanged(ConnectionType type);
}
