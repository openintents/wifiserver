package org.openintents.wifiserver.preference;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultInt;
import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultString;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref.Scope;

/**
 * Android Annotations wrapper for preferences. If a preference should appear in
 * preference activity and should still be accessible through this interface,
 * methods must be named like keys of preferences, which are defined in
 * values/strings_not_for_translations.xml. Default values are also located in
 * this file.
 *
 * @author Stanley Förster
 *
 */
@SharedPref(value = Scope.APPLICATION_DEFAULT)
public interface OiWiFiPreferences {

    /**
     * Defines if SSL should be used for secure HTTPS connections.
     *
     * @return <b>true</b> if SSL is enabled, <b>false</b> otherwise.
     */
    @DefaultBoolean(false)
    boolean sslEnable();

    /**
     * Defines the port on which the server should listen for incoming
     * connections if SSL is enabled.
     *
     * @return The port which should be used for SSL connections.
     */
    @DefaultInt(8081)
    int sslPort();

    /**
     * Defines the port on which the server should listen for incoming
     * connections if SSL is disabled.
     *
     * @return The port which should be used for non-SSL connections.
     */
    @DefaultInt(8080)
    int port();

    /**
     * Specifies if the user should be asked for a password to access the
     * server.
     * The default value is <b>false</b>
     *
     * @return <b>true</b> if the user should be asked for a password to access
     *         the server, <b>false</b> otherwise.
     */
    @DefaultBoolean(false)
    boolean passwordEnable();

    /**
     * Returns the password which was set by the user. It is salted and hashed,
     * so only this hash will be returned.
     *
     * @return The user defined password.
     */
    @DefaultString("12345")
    String customPassword();
}
