package org.openintents.wifiserver.webserver;

import org.openintents.wifiserver.webserver.WebServer.Status;

public interface ServerStatusListener {

    void onStatusChanged(Status status, String msg);

}
