package drsa.utils;

import java.security.SecureRandom;


/**
 * A set of useful functions.
 */
public class Utils {

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();


    /** Uses the Java SecureRandom class to produce random alphabetic strings
     * with the given length. Used for test purposes only.
     * @param len Desired string length.
     * @return A string with the given length.
     */
    public static String randomString(int len){
        StringBuilder sb = new StringBuilder(len);
        for(int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }
}