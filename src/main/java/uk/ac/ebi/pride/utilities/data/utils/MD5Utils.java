package uk.ac.ebi.pride.utilities.data.utils;

//~--- JDK imports ------------------------------------------------------------

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5Utils provides static method
 * <p/>
 * @author Rui Wang, Yasset Perez-Riverol
 * Date: 24/06/11
 * Time: 10:01
 */
public final class MD5Utils {

    /**
     * Private Constructor
     */
    private MD5Utils() {

    }

    /**
     * Generate md5 hash from a given string
     *
     * @param msg input string
     * @return String  md5 hash
     * @throws java.security.NoSuchAlgorithmException
     *          java.security.NoSuchAlgorithmException
     */
    public static String generateHash(String msg) throws NoSuchAlgorithmException {
        if (msg == null) {
            throw new IllegalArgumentException("Input string can not be null");
        }

        MessageDigest m = MessageDigest.getInstance("MD5");

        m.reset();
        m.update(msg.getBytes());

        byte[] digest = m.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashText = bigInt.toString(16);

        // zero pad to 32 chars
        while (hashText.length() < 32) {
            hashText = "0" + hashText;
        }

        return hashText;
    }
}



