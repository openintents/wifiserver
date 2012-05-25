package org.openintents.wifiserver;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultString;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref
public interface OiWiFiPreferences {

    @DefaultBoolean(false)
    boolean sslEnable();
    
    @DefaultString("8081")
    String sslPort();
    
    @DefaultString("8080")
    String port();
    
    @DefaultBoolean(false)
    boolean passwordEnable();
    
    @DefaultBoolean(false)
    boolean customPasswordEnable();
    
    @DefaultString("12345")
    String customPassword();
}
