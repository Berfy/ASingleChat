package cn.berfy.sdk.mvpbase.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC算法MD5签名
 */
public class Hmac {
    private static String TAG = "Hmac";

    public static String md5(String s) {
        try {
            return md5(s.getBytes("ASCII"));
        } catch (UnsupportedEncodingException e) {
            LogF.d(TAG, "-----hmac sign " + e.getMessage());
        }
        return null;
    }

    public static String md5(byte[] body) {
        String sEncodedString = null;
        try {
            SecretKeySpec key = new SecretKeySpec(("9t3PVj('8^8L66Y>3Km7'4R1").getBytes("UTF-8"), "HmacMD5");
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(key);

            byte[] bytes = mac.doFinal(body);

            StringBuffer hash = new StringBuffer();

            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            sEncodedString = hash.toString();
        } catch (UnsupportedEncodingException e) {
            LogF.d(TAG, "-----hmac sign " + e.getMessage());
        } catch (InvalidKeyException e) {
            LogF.d(TAG, "-----hmac sign " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            LogF.d(TAG, "-----hmac sign " + e.getMessage());
        }
        return sEncodedString;
    }
}
