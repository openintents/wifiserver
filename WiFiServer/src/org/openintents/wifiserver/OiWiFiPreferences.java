package org.openintents.wifiserver;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultInt;
import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultString;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref.Scope;

/**
 * Android Annotations wrapper for preferences. To work properly, methods must
 * be named like keys of preferences, which are defined in
 * values/strings_not_for_translations.xml
 * Default values are also located in this file.
 * 
 * @author Stanley Förster
 * 
 */
@SharedPref(value=Scope.APPLICATION_DEFAULT)
public interface OiWiFiPreferences {

    @DefaultBoolean(false)
    boolean sslEnable();
    
    @DefaultInt(8081)
    int sslPort();
    
    @DefaultInt(8080)
    int port();
    
    @DefaultBoolean(false)
    boolean passwordEnable();
    
    @DefaultBoolean(false)
    boolean customPasswordEnable();
    
    @DefaultString("12345")
    String customPassword();
}
