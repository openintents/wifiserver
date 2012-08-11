package org.openintents.wifiserver.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.openintents.wifiserver.OIWiFiServerActivity_;
import org.openintents.wifiserver.R;

import com.jayway.android.robotium.solo.Solo;

import android.app.Activity;
import android.content.Intent;
import android.test.InstrumentationTestCase;

public abstract class BasicServerTest extends InstrumentationTestCase {
    
    private Solo solo;
    private Activity activity;

    protected static final String baseURL = "http://127.0.0.1:8080";
    
    private String getString(int resId) {
        return activity.getString(resId);
    }

    private void startServer() {
        solo.assertCurrentActivity(
                "Expected " + OIWiFiServerActivity_.class.getSimpleName()
                        + " activity!", OIWiFiServerActivity_.class);
        if (solo.searchToggleButton(getString(R.string.startServer))) {
            solo.clickOnToggleButton(getString(R.string.startServer));
            solo.waitForText(getString(R.string.stopServer));
        }

    }

    private void stopServer() {
        solo.assertCurrentActivity(
                "Expected " + OIWiFiServerActivity_.class.getSimpleName()
                        + " activity!", OIWiFiServerActivity_.class);
        if (solo.searchToggleButton(getString(R.string.stopServer))) {
            solo.clickOnToggleButton(getString(R.string.stopServer));
            solo.waitForText(getString(R.string.startServer));
        }
    }

    private void startWiFiActivity() {
        Intent i = new Intent();
        i.setAction("android.intent.action.MAIN");
        i.setClassName(OIWiFiServerActivity_.class.getPackage().getName(),
                OIWiFiServerActivity_.class.getCanonicalName());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        activity = getInstrumentation().startActivitySync(i);

        this.solo = new Solo(getInstrumentation(), activity);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        startWiFiActivity();
        startServer();
    }

    @Override
    protected void tearDown() {
        stopServer();
        solo.finishOpenedActivities();
    }

    protected HttpResponse doGet(String url) {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        try {
            HttpResponse response = client.execute(request);
            return response;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected HttpResponse doPost(String url, Map<String, String> parameters) {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);

        try {
            
            if (parameters != null) {
                List<NameValuePair> nvpList = new ArrayList<NameValuePair>(
                        parameters.size());
                for (String key : parameters.keySet()) {
                    nvpList.add(new BasicNameValuePair(key, parameters.get(key)));
                }
                
                request.setEntity(new UrlEncodedFormEntity(nvpList));
            }

            request.setEntity(new StringEntity("blank"));
            HttpResponse response = client.execute(request);
            return response;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
