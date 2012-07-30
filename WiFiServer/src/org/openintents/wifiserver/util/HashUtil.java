package org.openintents.wifiserver.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * This class includes some methods which simplifies the use of hash mechanisms.
 *
 * @author Stanley Förster
 *
 */
public class HashUtil {

    /**
     * Length of a salt.
     */
    public final static int SALT_LENGTH = 8;

    /**
     * Private constructor to avoid instantiation.
     */
    private HashUtil() {

    }

    /**
     * Calculated the SHA-256 hash of the given input string.
     * The input string is converted to a byte array which. This is then hashed
     * and converted back to a hex encoded hash string.
     *
     * @param input
     *            The string that should be hashed.
     * @return A hex representation of the hashed string.
     */
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

    /**
     * Generates a salt using {@link UUID} of length {@value #SALT_LENGTH}
     *
     * @return A salt of length {@value #SALT_LENGTH}
     */
    public static String generateSalt() {
        return UUID.randomUUID().toString().substring(0, SALT_LENGTH);
    }
}
