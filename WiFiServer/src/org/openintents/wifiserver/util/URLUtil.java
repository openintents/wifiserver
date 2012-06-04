package org.openintents.wifiserver.util;

import android.net.Uri;

public class URLUtil {

    private URLUtil() {}
    
    public static String getParameter(String uri, String paramName) {
        return getParameter(Uri.parse(uri), paramName);
    }
    
    public static String getParameter(Uri uri, String paramName) {
        return uri.getQueryParameter(paramName);
    }
}
