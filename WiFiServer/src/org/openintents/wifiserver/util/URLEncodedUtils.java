package org.openintents.wifiserver.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * This class includes an updated version of
 * {@link org.apache.http.client.utils.URLEncodedUtils#parse(HttpEntity)}
 * method. The source of this implementation is Apache httpcomponents client
 * library 4.0.1.
 */
public class URLEncodedUtils {
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    /**
     * Returns a list of {@link NameValuePair NameValuePairs} as parsed from an
     * {@link HttpEntity}. The encoding is taken from the entity's
     * Content-Encoding header.
     * <p>
     * This is typically used while parsing an HTTP POST.
     *</p>
     * @param entity
     *            The entity to parse
     * @throws IOException
     *             If there was an exception getting the entity's data.
     */
    public static List<NameValuePair> parse (
            final HttpEntity entity) throws IOException {
        List <NameValuePair> result = Collections.emptyList();

        String contentType = null;
        String charset = null;

        Header h = entity.getContentType();
        if (h != null) {
            HeaderElement[] elems = h.getElements();
            if (elems.length > 0) {
                HeaderElement elem = elems[0];
                contentType = elem.getName();
                NameValuePair param = elem.getParameterByName("charset");
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }

        if (contentType != null && contentType.equalsIgnoreCase(CONTENT_TYPE)) {
            final String content = EntityUtils.toString(entity, HTTP.ASCII);
            if (content != null && content.length() > 0) {
                result = new ArrayList <NameValuePair>();
                org.apache.http.client.utils.URLEncodedUtils.parse(result, new Scanner(content), charset);
            }
        }
        return result;
    }
}
