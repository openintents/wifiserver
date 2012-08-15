package org.openintents.wifiserver.requesthandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;
import android.content.pm.PackageInfo;

/**
 * Handler which is used to get a list of available and supported apps. It handles requests of the form "/apps".
 *
 * @author Stanley Förster
 *
 */
public class AvailableAppsHandler implements HttpRequestHandler {

    /**
     * The application's context, which is used to retrieve a list of all installed apps.
     */
    private final Context mContext;

    /**
     * Creates a new handler.
     *
     * @param context The application's context.
     */
    public AvailableAppsHandler(Context context) {
        mContext = context;
    }

    /**
     * <p>
     * {@inheritDoc}
     * </p>
     *
     * This handler handles only GET requests. It returns a comma separated
     * list, which contains the package names of all apps, that are installed on
     * the device and supported by the wifiserver app.
     */
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        if (!"GET".equals(request.getRequestLine().getMethod())) {
            response.setStatusCode(405);
            return;
        }

        Set<String> availableApps = getAvailableApps();
        String content = "";

        boolean firstEntry = true;

        for (String app : availableApps) {
            if (!firstEntry) {
                content += ",";
            }
            firstEntry = false;
            content += app;
        }

        response.setEntity(new StringEntity(content));
    }

    /**
     * Creates a set of available and supported apps. It collects a list of all
     * apps that are installed on the devices and compares this list with a list
     * of apps, which are supported by the wifiserver app.
     * The set contains the package names of those applications.
     *
     * @return A set of available and supported apps.
     */
    private Set<String> getAvailableApps() {
        Set<String> result = new HashSet<String>();

        for (PackageInfo pack : mContext.getPackageManager().getInstalledPackages(0)) {
            if (pack.packageName.equals("org.openintents.notepad") ||
                pack.packageName.equals("org.openintents.shopping")
               )
                result.add(pack.packageName);
        }

        return result;
     }
}
