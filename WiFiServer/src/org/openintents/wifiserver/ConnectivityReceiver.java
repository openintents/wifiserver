package org.openintents.wifiserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class ConnectivityReceiver extends BroadcastReceiver {

    enum ConnectionType {
        NET_3G,
        NET_WIFI,
        NET_NONE    
    }
    
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

    public abstract void onConnectionChanged(ConnectionType type);
}
