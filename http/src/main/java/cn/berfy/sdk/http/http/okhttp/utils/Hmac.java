package cn.berfy.sdk.http.http.okhttp.utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * HMAC算法MD5签名
 */
public class Hmac {
    private static String TAG = "Hmac";

    public static String md5(String s) {
        try {
            if (TextUtils.isEmpty(s)) {
                s = "";
            }
            return md5(s.getBytes("ASCII"));
        } catch (UnsupportedEncodingException e) {
            HLogF.d(TAG, "-----hmac sign " + e.getMessage());
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
        } catch (Exception e) {
            HLogF.d(TAG, "-----hmac sign " + e.getMessage());
        }
        return sEncodedString;
    }
}
