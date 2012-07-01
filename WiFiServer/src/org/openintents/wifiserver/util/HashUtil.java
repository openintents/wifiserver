package org.openintents.wifiserver.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class HashUtil {

    public final static int SALT_LENGTH = 8;

    public HashUtil() {

    }

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes =  md.digest(input.getBytes());

            StringBuilder output = new StringBuilder(hashedBytes.length);

            for (int i=0; i<hashedBytes.length; i++) {
                String hex = Integer.toHexString(0xFF & hashedBytes[i]);
                if (hex.length()==1)
                    hex= "0"+hex;
                output.append(hex);
            }

            return output.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String generateSalt() {
        return UUID.randomUUID().toString().substring(0, SALT_LENGTH);
    }
}
