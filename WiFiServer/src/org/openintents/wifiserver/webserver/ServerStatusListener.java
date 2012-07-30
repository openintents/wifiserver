package org.openintents.wifiserver.webserver;

import org.openintents.wifiserver.webserver.WebServer.Status;

/**
 * Listener that is used to notice changes of web server's state.
 *
 * @author Stanley Förster
 *
 */
public interface ServerStatusListener {

    /**
     * This method is called, after the web server changed its state.
     *
     * @param status
     *            the new state of the server.
     * @param msg
     *            an optional message which includes more information about the
     *            new state.
     */
    void onStatusChanged(Status status, String msg);

}
