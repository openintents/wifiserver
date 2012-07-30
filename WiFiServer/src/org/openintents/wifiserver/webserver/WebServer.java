package org.openintents.wifiserver.webserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.nio.DefaultServerIOEventDispatch;
import org.apache.http.impl.nio.SSLServerIOEventDispatch;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.nio.protocol.BufferingHttpServiceHandler;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.util.Log;

/**
 * The WebServer is the core of the server application. It handles the
 * initialization and is used to start and stop the actual server component.
 *
 * @author Stanley Förster
 *
 */
public class WebServer {

    /**
     * This enum includes all states, the server can have.
     *
     * @author Stanley Förster
     */
    public enum Status {
        STARTED,
        STOPPED,
        ERROR
    }

    private final static String        TAG   = WebServer.class.getSimpleName();
    private int                        mPort = -1;

    private ListeningIOReactor         mIOReactor;
    private IOEventDispatch            mIOEventDispatch;
    private HttpRequestHandlerRegistry mHandlerRegistry;
    private BasicHttpProcessor         mHttpProcessor;
    private List<ServerStatusListener> mListeners;

    /**
     * Creates a new {@link WebServer} with SSL support.<br />
     * When using this constructor the web server is initialized with SSL
     * support using the given certificate. To load the key store a password is
     * required. Also only BKS key stores are supported.<br />
     * The server will listen for incoming requests only on the specified port.
     *
     * @param port
     *            Port on which the server will listen for incoming requests.
     * @param enableSSL
     *            Indicates if communication should be SSL encrypted.
     * @param certFile
     *            The certificate which is used to establish a SSL connection.
     * @param password
     *            Password which is required to load the key store.
     */
    public WebServer(int port, boolean enableSSL, InputStream certFile, char[] password) {
        this.mPort = port;
        this.mListeners = new LinkedList<ServerStatusListener>();

        HttpParams httpParams = new BasicHttpParams();
        httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 30000)
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8*1024)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "OI Server");

        mHttpProcessor = new BasicHttpProcessor();
        mHttpProcessor.addInterceptor(new ResponseDate());
        mHttpProcessor.addInterceptor(new ResponseServer());
        mHttpProcessor.addInterceptor(new ResponseContent());
        mHttpProcessor.addInterceptor(new ResponseConnControl());

        BufferingHttpServiceHandler handler = new BufferingHttpServiceHandler(
                        mHttpProcessor,
                        new DefaultHttpResponseFactory(),
                        new DefaultConnectionReuseStrategy(),
                        httpParams);

        mHandlerRegistry = new HttpRequestHandlerRegistry();
        handler.setHandlerResolver(mHandlerRegistry);

        if (enableSSL) {
            mIOEventDispatch = new SSLServerIOEventDispatch(handler, createSSLContext(certFile, password), httpParams);
        } else {
            mIOEventDispatch = new DefaultServerIOEventDispatch(handler, httpParams);
        }

        try {
            mIOReactor = new DefaultListeningIOReactor(2, httpParams);
        } catch (IOReactorException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a web server without SSL support, which will listen on the
     * specified port.
     *
     * @param port
     *            The port on which the server should listen for incoming
     *            requests.
     */
    public WebServer(int port) {
        this(port, false, null, null);
    }

    /**
     * Adds a new request interceptor, which will be called before the request
     * is handled by a specific {@link HttpRequestHandler}.
     *
     * @param interceptor
     *            The interceptor which should be added to the list of
     *            interceptors.
     */
    public void addRequestInterceptor(HttpRequestInterceptor interceptor) {
        mHttpProcessor.addInterceptor(interceptor);
    }

    /**
     * Registers a new request handler which will be invoked when a request's
     * URL matches the given URL pattern.
     * If there are more than one matching pattern, the most specific one will
     * be used.
     *
     * @param urlPattern
     *            The pattern that indicates which requests should be handled by
     *            the request handler.
     * @param handler
     *            The handler which will be invoked if a request's URL matches
     *            the corresponding pattern.
     */
    public void registerRequestHandler(String urlPattern, HttpRequestHandler handler) {
        mHandlerRegistry.register(urlPattern, handler);
    }

    /**
     * Creates a new SSL context by loading the given certificate from a key
     * store. The only supported format is "BKS".
     *
     * @param certFile
     *            Certificate file which is required to create a SSL context.
     * @param password
     *            The password is required to load the key store.
     * @return A initializes SSL context using the given certificate.
     */
    private SSLContext createSSLContext(InputStream certFile, char[] password) {
        SSLContext sslContext = null;
        try {
            KeyStore keyStore;
            keyStore = KeyStore.getInstance("BKS");
            keyStore.load(certFile, password);
            certFile.close();

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, password);

            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
        } catch (KeyStoreException e) {
            Log.e(TAG, "There was an error with the keystore!", e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "No such algorithm!", e);
        } catch (CertificateException e) {
            Log.e(TAG, "Failed to initialize keystore!", e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read certification file!", e);
        } catch (UnrecoverableKeyException e) {
            Log.e(TAG, "Failed to init key manager factory!", e);
        } catch (KeyManagementException e) {
            Log.e(TAG, "Failed to init ssl context!", e);
        }

        return sslContext;
    }

    /**
     * Starts the server which will then listen for incoming requests. This will
     * run in a new thread. After the server started successfully the state will
     * be changed to "STARTED".
     */
    public void start() {
        Thread server = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mIOReactor.listen(new InetSocketAddress(mPort));
                    mIOReactor.execute(mIOEventDispatch);
                } catch (IOException e) {
                    e.printStackTrace();
                    statusUpdate(Status.ERROR, e.toString());
                }
            }
        });
        server.setDaemon(true);
        server.start();

        statusUpdate(Status.STARTED);
    }

    /**
     * Stops the server and sets its state to "STOPPED".
     */
    public void stop() {
        try {
            mIOReactor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }

        statusUpdate(Status.STOPPED);
    }

    /**
     * Returns the port which was specified in the constructor.
     *
     * @return The port, the server listens on.
     */
    public int getPort() {
        return mPort;
    }

    /**
     * Adds a new listener which will be notified if the server changes its
     * state.
     *
     * @param listener
     *            A new listener.
     */
    public void addListener(ServerStatusListener listener) {
        this.mListeners.add(listener);
    }

    /**
     * Removes the specified listener, so it will be no longer invoked after the
     * server changed its state.
     *
     * @param listener
     *            The listener which should be removed.
     */
    public void removeListener(ServerStatusListener listener) {
        this.mListeners.remove(listener);
    }

    /**
     * Notifies all registered listeners if the server's state changed.
     * An optional message can be given which includes additional information
     * about the state change, like an error message.
     *
     * @param status
     *            The server's new state.
     * @param msg
     *            An optional message.
     */
    private void statusUpdate(Status status, String msg) {
        for (ServerStatusListener listener : mListeners) {
            listener.onStatusChanged(status, msg);
        }
    }

    /**
     * Notifies all registered listeners if the server's state changed.
     *
     * @param status
     *            The server's new state.
     */
    private void statusUpdate(Status status) {
        this.statusUpdate(status, null);
    }
}
