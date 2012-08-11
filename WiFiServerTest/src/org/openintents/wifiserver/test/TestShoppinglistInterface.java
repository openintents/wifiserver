package org.openintents.wifiserver.test;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openintents.wifiserver.util.StringUtil;

public class TestShoppinglistInterface extends BasicServerTest {
    
    public void testDeleteAllLists() throws IllegalStateException, IOException {
        HttpResponse response = doGet(baseURL+"/shoppinglist/list/delete");

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
        
        response = doGet(baseURL+"/shoppinglist/list/get");
        
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(2, response.getEntity().getContentLength());
        assertEquals("[]", StringUtil.fromInputStream(response.getEntity().getContent()));
    }
    
    public void testCreateAndGetList() throws IllegalStateException, IOException, JSONException {
        String testlist = "Testlist";
        
        doGet(baseURL+"/shoppinglist/list/delete");
        HttpResponse response = doGet(baseURL+"/shoppinglist/list/new?name="+testlist);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
        
        response = doGet(baseURL+"/shoppinglist/list/get");
        
        assertEquals(200, response.getStatusLine().getStatusCode());

        String stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        JSONArray jsonArray = new JSONArray(stringArray);

        assertEquals(1, jsonArray.length());

        JSONObject actualList = jsonArray.getJSONObject(0);

        assertEquals(actualList.getString("name"), testlist);
    }
    
    public void testCreateListWithoutName() throws IllegalStateException, IOException, JSONException {
        doGet(baseURL+"/shoppinglist/list/delete");
        HttpResponse response = doGet(baseURL+"/shoppinglist/list/new");

        assertEquals(400, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
        
        response = doGet(baseURL+"/shoppinglist/list/get");
        
        assertEquals(200, response.getStatusLine().getStatusCode());

        String stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        JSONArray jsonArray = new JSONArray(stringArray);

        assertEquals(0, jsonArray.length());
    }
    
    public void testCreateListWithEmptyName() throws IllegalStateException, IOException, JSONException {
        String testlist = "";
        
        doGet(baseURL+"/shoppinglist/list/delete");
        HttpResponse response = doGet(baseURL+"/shoppinglist/list/new?name="+testlist);

        assertEquals(400, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
        
        response = doGet(baseURL+"/shoppinglist/list/get");
        
        assertEquals(200, response.getStatusLine().getStatusCode());

        String stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        JSONArray jsonArray = new JSONArray(stringArray);

        assertEquals(1, jsonArray.length());
    }
    
    public void testCreateExistingList() throws IllegalStateException, IOException, JSONException {
        String testlist = "Testlist";
        
        doGet(baseURL+"/shoppinglist/list/delete");
        HttpResponse response = doGet(baseURL+"/shoppinglist/list/new?name="+testlist);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
        
        response = doGet(baseURL+"/shoppinglist/list/new?name="+testlist);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(0, response.getEntity().getContentLength());
        
        response = doGet(baseURL+"/shoppinglist/list/get");
        assertEquals(200, response.getStatusLine().getStatusCode());
        String stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        JSONArray jsonArray = new JSONArray(stringArray);
        assertEquals(1, jsonArray.length());
    }
    
    public void testCreateAndGetLists() throws IllegalStateException, IOException, JSONException {
        String testlist_1 = "Testlist_1";
        String testlist_2 = "Testlist_2";
        
        doGet(baseURL+"/shoppinglist/list/delete");
        doGet(baseURL+"/shoppinglist/list/new?name="+testlist_1);
        doGet(baseURL+"/shoppinglist/list/new?name="+testlist_2);
        
        HttpResponse response = doGet(baseURL+"/shoppinglist/list/get");
        
        assertEquals(200, response.getStatusLine().getStatusCode());

        String stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        JSONArray jsonArray = new JSONArray(stringArray);

        assertEquals(2, jsonArray.length());

        JSONObject actualList_1 = jsonArray.getJSONObject(0);
        JSONObject actualList_2 = jsonArray.getJSONObject(1);

        assertTrue( (actualList_1.getString("name").equals(testlist_1) && actualList_2.getString("name").equals(testlist_2)) ||
                    (actualList_2.getString("name").equals(testlist_1) && actualList_1.getString("name").equals(testlist_2)) );
    }
    
    public void testDeleteSpecificList() throws IllegalStateException, IOException, JSONException {
        String beforeRename = "List1";
        
        doGet(baseURL+"/shoppinglist/list/delete");
        doGet(baseURL+"/shoppinglist/list/new?name="+beforeRename);
        
        HttpResponse response = doGet(baseURL+"/shoppinglist/list/get");
        String stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        JSONArray jsonArray = new JSONArray(stringArray);
        assertEquals(1, jsonArray.length());

        JSONObject list = jsonArray.getJSONObject(0);

        String id = list.getString("_id");
        
        
        response = doGet(baseURL+"/shoppinglist/list/delete?id="+id);
        assertEquals(200, response.getStatusLine().getStatusCode());

        response = doGet(baseURL+"/shoppinglist/list/get");
        
        stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        jsonArray = new JSONArray(stringArray);
        assertEquals(0, jsonArray.length());
    }
    
    public void testRenameListByName() throws IllegalStateException, IOException, JSONException {
        String beforeRename = "List1";
        String afterRename = "List2";
        
        doGet(baseURL+"/shoppinglist/list/delete");
        doGet(baseURL+"/shoppinglist/list/new?name="+beforeRename);
        
        HttpResponse response = doGet(baseURL+"/shoppinglist/list/rename?oldname="+beforeRename+"&newname="+afterRename);
        assertEquals(200, response.getStatusLine().getStatusCode());

        response = doGet(baseURL+"/shoppinglist/list/get");
        
        String stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        JSONArray jsonArray = new JSONArray(stringArray);
        assertEquals(1, jsonArray.length());

        JSONObject afterList = jsonArray.getJSONObject(0);

        assertEquals(afterList.getString("name"), afterRename);
    }
    
    public void testRenameListById() throws IllegalStateException, IOException, JSONException {
        String beforeRename = "List1";
        String afterRename = "List2";
        
        doGet(baseURL+"/shoppinglist/list/delete");
        doGet(baseURL+"/shoppinglist/list/new?name="+beforeRename);
        
        HttpResponse response = doGet(baseURL+"/shoppinglist/list/get");
        String stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        JSONArray jsonArray = new JSONArray(stringArray);
        assertEquals(1, jsonArray.length());

        JSONObject list = jsonArray.getJSONObject(0);

        String id = list.getString("_id");
        
        response = doGet(baseURL+"/shoppinglist/list/rename?id="+id+"&newname="+afterRename);
        assertEquals(200, response.getStatusLine().getStatusCode());

        response = doGet(baseURL+"/shoppinglist/list/get");
        
        stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        jsonArray = new JSONArray(stringArray);
        assertEquals(1, jsonArray.length());

        list = jsonArray.getJSONObject(0);

        assertEquals(list.getString("name"), afterRename);
    }
    
    public void testRenameListByIdAndName() throws IllegalStateException, IOException, JSONException {
        String beforeRename = "List1";
        String afterRename = "List2";
        
        doGet(baseURL+"/shoppinglist/list/delete");
        doGet(baseURL+"/shoppinglist/list/new?name="+beforeRename);
        
        HttpResponse response = doGet(baseURL+"/shoppinglist/list/get");
        String stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        JSONArray jsonArray = new JSONArray(stringArray);
        assertEquals(1, jsonArray.length());

        JSONObject list = jsonArray.getJSONObject(0);

        String id = list.getString("_id");
        
        response = doGet(baseURL+"/shoppinglist/list/rename?id="+id+"&newname="+afterRename+"&oldname="+beforeRename);
        assertEquals(400, response.getStatusLine().getStatusCode());

        response = doGet(baseURL+"/shoppinglist/list/get");
        
        stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        jsonArray = new JSONArray(stringArray);
        assertEquals(1, jsonArray.length());

        list = jsonArray.getJSONObject(0);

        assertEquals(list.getString("name"), beforeRename);
    }
    
    public void testRenameListWithoutName() throws IllegalStateException, IOException, JSONException {
        String beforeRename = "List1";
        
        doGet(baseURL+"/shoppinglist/list/delete");
        doGet(baseURL+"/shoppinglist/list/new?name="+beforeRename);
        
        HttpResponse response = doGet(baseURL+"/shoppinglist/list/rename?oldname="+beforeRename);
        assertEquals(400, response.getStatusLine().getStatusCode());

        response = doGet(baseURL+"/shoppinglist/list/get");
        
        String stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        JSONArray jsonArray = new JSONArray(stringArray);
        assertEquals(1, jsonArray.length());

        JSONObject list = jsonArray.getJSONObject(0);

        assertEquals(list.getString("name"), beforeRename);
    }
    
    public void testRenameListWithEmptyName() throws IllegalStateException, IOException, JSONException {
        String beforeRename = "List1";
        String afterRename = "";
        
        doGet(baseURL+"/shoppinglist/list/delete");
        doGet(baseURL+"/shoppinglist/list/new?name="+beforeRename);
        
        HttpResponse response = doGet(baseURL+"/shoppinglist/list/rename?newname="+afterRename+"&oldname="+beforeRename);
        assertEquals(400, response.getStatusLine().getStatusCode());

        response = doGet(baseURL+"/shoppinglist/list/get");
        
        String stringArray = StringUtil.fromInputStream(response.getEntity().getContent());
        JSONArray jsonArray = new JSONArray(stringArray);
        assertEquals(1, jsonArray.length());

        JSONObject list = jsonArray.getJSONObject(0);

        assertEquals(list.getString("name"), beforeRename);
    }
}
