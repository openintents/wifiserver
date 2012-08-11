package org.openintents.wifiserver.test;

import org.apache.http.HttpResponse;

public class TestHTTPMethods extends BasicServerTest {
    
    private String[][] parameters = new String[][] {
            { "/",                         "GET"  },
            { "/login",                    "POST" },
            { "/logout",                   "GET"  },
            { "/notes/get",                "GET"  },
            { "/notes/new",                "POST" },
            { "/notes/delete",             "GET"  },
            { "/notes/update",             "POST" },
            { "/shoppinglist/list/get",    "GET"  },
            { "/shoppinglist/list/delete", "GET"  },
            { "/shoppinglist/list/new",    "GET"  },
            { "/shoppinglist/list/rename", "GET"  },
            { "/shoppinglist/item/get",    "GET"  },
            { "/shoppinglist/item/update", "POST" },
            { "/shoppinglist/list/delete", "GET"  }
    };
    
    public void testHTTPMethods() {
        for (String[] params : parameters) {
            HttpResponse response = null;
            if (params[1].equals("GET"))
                response = doPost(baseURL+params[0], null);
            else 
                response = doGet(baseURL+params[0]);

            assertEquals("call: "+params[0], 405, response.getStatusLine().getStatusCode());
        }
    }
}
