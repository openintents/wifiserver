package org.openintents.wifiserver.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openintents.wifiserver.OIWiFiServerActivity_;
import org.openintents.wifiserver.R;
import org.openintents.wifiserver.util.StringUtil;

import android.app.Activity;
import android.content.Intent;
import android.test.InstrumentationTestCase;

import com.jayway.android.robotium.solo.Solo;

public class TestNotesInterface extends InstrumentationTestCase {

    private Solo solo;
    private static final String TAG = TestNotesInterface.class.getSimpleName();
    private Activity activity;
    
    private Random rand = new Random();
    
    private String getString(int resId) {
        return activity.getString(resId);
    }
    
    private void startServer() {
        solo.assertCurrentActivity("Expected "+OIWiFiServerActivity_.class.getSimpleName()+" activity!", OIWiFiServerActivity_.class);
        if (solo.searchToggleButton(getString(R.string.startServer))) {
            solo.clickOnToggleButton(getString(R.string.startServer));
            solo.waitForText(getString(R.string.stopServer));
        }
    }
    
    private void stopServer() {
        solo.assertCurrentActivity("Expected "+OIWiFiServerActivity_.class.getSimpleName()+" activity!", OIWiFiServerActivity_.class);
        if (solo.searchToggleButton(getString(R.string.stopServer))) {
            solo.clickOnToggleButton(getString(R.string.stopServer));
            solo.waitForText(getString(R.string.startServer));
        }
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent i = new Intent();
        i.setAction("android.intent.action.MAIN");
        i.setClassName(OIWiFiServerActivity_.class.getPackage().getName(), OIWiFiServerActivity_.class.getCanonicalName());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        activity = getInstrumentation().startActivitySync(i);

        this.solo = new Solo(getInstrumentation(), activity);
        
        startServer();
    }
    
    @Override
    protected void tearDown() {
        stopServer();
        solo.finishOpenedActivities();
    }
    
    private HttpResponse doGet(String url) {
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
    
    private HttpResponse doPost(String url, NameValuePair... nameValuePairs) {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);
        
        try {
            request.setEntity(new UrlEncodedFormEntity(Arrays.asList(nameValuePairs)));
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
    
    /**
     * Test case:
     *  user deletes all notes
     *  
     * Result:
     *  status code = 200
     *  content is empty
     */
    public void testDeleteNote() {
        HttpResponse response = doGet("http://127.0.0.1:8080/notes/delete");
        
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
    }
    
    /**
     * Test case:
     *  user deletes all notes and then requests a list of all notes
     *  
     * Result:
     *  status code = 200
     *  content = "[]"
     */
    public void testDeleteAndGetNote() {
        doGet("http://127.0.0.1:8080/notes/delete");
        
        HttpResponse response = doGet("http://127.0.0.1:8080/notes/get");
        
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(2, response.getEntity().getContentLength());
        try {
            assertEquals("[]", StringUtil.fromInputStream(response.getEntity().getContent()));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Test case:
     *  user creates note
     *  
     * Result:
     *  status code = 200
     *  content is empty
     */
    public void testCreateNote() {
        doGet("http://127.0.0.1:8080/notes/delete");
        
        String title = "TestTitle_"+rand.nextInt(1000);
        String note = "TestNote_"+rand.nextInt(1000);
        
        HttpResponse response = doPost("http://127.0.0.1:8080/notes/new", new NameValuePair[] {
           new BasicNameValuePair("title", title),
           new BasicNameValuePair("note", note)
        });
        
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
    }
    
    /**
     * Test case:
     *  user creates note and requests a list of notes, which should contain the previously created note
     *  
     * Result:
     *  status code = 200
     *  content is JSON array of JSON objects which contains the new note
     */
    public void testCreateAndGetNote() {
        String title = "TestTitle_"+rand.nextInt(1000);
        String note = "TestNote_"+rand.nextInt(1000);
        
        doPost("http://127.0.0.1:8080/notes/new", new NameValuePair[] {
           new BasicNameValuePair("title", title),
           new BasicNameValuePair("note", note)
        });
        
        HttpResponse response = doGet("http://127.0.0.1:8080/notes/get");
        
        boolean foundNote = false;
        
        try {
            String stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
            JSONArray jsonArray = new JSONArray(stringArray);
            
            for (int i = 0; i<jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                
                if (jsonObject.getString("title").equals(title) && jsonObject.getString("note").equals(note)) {
                    foundNote = true;
                }
            }
            
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        assertTrue("New note has not been returned by get request", foundNote);
    }
    
    /**
     * Test case:
     *  user deletes all notes and requests a particular note by id
     *  
     * Result:
     *  status code = 404
     *  content is empty
     */
    public void testDeleteAndGetParticularNote() {
        doGet("http://127.0.0.1:8080/notes/delete");

        HttpResponse response = doGet("http://127.0.0.1:8080/notes/get?id="+rand.nextInt(1000));

        assertEquals(404, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
    }
    
    /**
     * Test case:
     *  new note should be created with note parameter missing
     *  
     * Result:
     *  status code = 400
     *  content is empty
     */
    public void testCreateMissingNoteParam() {
        String title = "TestTitle_"+rand.nextInt(1000);
        
        HttpResponse response = doPost("http://127.0.0.1:8080/notes/new", new NameValuePair[] {
           new BasicNameValuePair("title", title),
        });
        
        assertEquals(400, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
    }
    
    /**
     * Test case:
     *  new note should be created with title parameter missing
     *  
     * Result:
     *  status code = 400
     *  content is empty
     */
    public void testCreateMissingTitleParam() {
        String note = "TestNote_"+rand.nextInt(1000);
        
        HttpResponse response = doPost("http://127.0.0.1:8080/notes/new", new NameValuePair[] {
           new BasicNameValuePair("note", note),
        });
        
        assertEquals(400, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
    }
}
