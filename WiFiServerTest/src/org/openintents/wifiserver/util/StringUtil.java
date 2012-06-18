package org.openintents.wifiserver.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StringUtil {

    private StringUtil() {};
    
    public static String fromInputStream(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        
        if (inputStream != null) {
            String line = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader((inputStream)));
            while (null != (line = reader.readLine())) {
                result.append(line); 
            }
        }
        
        return result.toString();
    }
}
