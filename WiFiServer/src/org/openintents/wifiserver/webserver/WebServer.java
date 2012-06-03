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

public class WebServer {

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
    
    public void addRequestInterceptor(HttpRequestInterceptor interceptor) {
        mHttpProcessor.addInterceptor(interceptor);
    }
    
    public void registerRequestHandler(String urlPattern, HttpRequestHandler handler) {
        mHandlerRegistry.register(urlPattern, handler);
    }
    
    public WebServer(int port) {
        this(port, false, null, null);
    }
    
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
    
    public void start() {
        Thread server = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Server thread started");
                try {
                    mIOReactor.listen(new InetSocketAddress(mPort));
                    mIOReactor.execute(mIOEventDispatch);
                } catch (IOException e) {
                    e.printStackTrace();
                    statusUpdate(Status.ERROR, e.toString());
                }
                Log.d(TAG, "Server thread stopped");
            }
        });
        server.setDaemon(true);
        server.start();
        
        statusUpdate(Status.STARTED);
    }

    public void stop() {
        try {
            mIOReactor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        statusUpdate(Status.STOPPED);
    }

    public int getPort() {
        return mPort;
    }
    
    public void addListener(ServerStatusListener listener) {
        this.mListeners.add(listener);
    }
    
    public void removeListener(ServerStatusListener listener) {
        this.mListeners.remove(listener);
    }
    
    private void statusUpdate(Status status, String msg) {
        for (ServerStatusListener listener : mListeners) {
            listener.onStatusChanged(status, msg);
        }
    }
    
    private void statusUpdate(Status status) {
        this.statusUpdate(status, null);
    }
}
