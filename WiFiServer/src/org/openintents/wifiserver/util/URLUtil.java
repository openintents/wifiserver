package org.openintents.wifiserver.util;

import android.net.Uri;

/**
 * URLUtils is a container for methods that simplify the work with URLs.
 *
 * @author Stanley Förster
 *
 */
public class URLUtil {

    /**
     * Private constructor to avoid instantiation.
     */
    private URLUtil() {}

    /**
     * Extracts the value of the given GET parameters. If there are multiple
     * parameters of the same name, the first one is returned.
     *
     * @param uri
     *            A string representation of the URL.
     * @param paramName
     *            Name of the parameter.
     * @return Parameter's value or <code>null</code> if the parameter isn't present.
     *
     * @see Uri.getQueryParameter
     */
    public static String getParameter(String uri, String paramName) {
        return getParameter(Uri.parse(uri), paramName);
    }

    /**
     * Extracts the value of the given GET parameters. If there are multiple
     * parameters of the same name, the first one is returned.
     *
     * @param uri
     *            URL, which includes the parameter.
     * @param paramName
     *            Name of the parameter.
     * @return Parameter's value or <code>null</code> if the parameter isn't present.
     *
     * @see Uri.getQueryParameter
     */
    public static String getParameter(Uri uri, String paramName) {
        return uri.getQueryParameter(paramName);
    }
}
