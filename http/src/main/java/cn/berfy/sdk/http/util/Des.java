package cn.berfy.sdk.http.util;

import android.util.Base64;

import cn.berfy.sdk.http.http.okhttp.utils.HLogF;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class Des {

    private final static String TAG = "Des加密";
//    private final static String HEX = "0123456789ABCDEF";
    private final static String WORK_MODE = "/CBC";
    private final static String MODE_TIANCHONG = "/PKCS5Padding";
    private final static String IVPARAMETERSPEC = "12345678";////初始化向量参数，AES 为16bytes. DES 为8bytes.
    private final static String ALGORITHM = "desede";//DES是加密方式
//    private static final String SHA1PRNG = "SHA1PRNG";//// SHA1PRNG 强随机种子算法, 要区别4.2以上版本的调用方法
//    private static final String ENCODING = "UTF-8";//// SHA1PRNG 强随机种子算法, 要区别4.2以上版本的调用方法

//    public static String generateKey() {
//        try {
//            SecureRandom localSecureRandom = SecureRandom.getInstance(SHA1PRNG);
//            byte[] bytes_key = new byte[20];
//            localSecureRandom.nextBytes(bytes_key);
//            String str_key = toHex(bytes_key);
//            return str_key;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    //二进制转字符
//    public static String toHex(byte[] buf) {
//        if (buf == null)
//            return "";
//        StringBuffer result = new StringBuffer(2 * buf.length);
//        for (int i = 0; i < buf.length; i++) {
//            appendHex(result, buf[i]);
//        }
//        return result.toString();
//    }

//    private static void appendHex(StringBuffer sb, byte b) {
//        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
//    }

    // 对密钥进行处理
    private static Key getRawKey(String key) throws Exception {
        if (ALGORITHM.indexOf("desede") != -1) {//3DES加密
            HLogF.d(TAG, "3DES方式");
            DESedeKeySpec deSedeKeySpec = new DESedeKeySpec(key.getBytes());
            SecretKeyFactory keyfactory = SecretKeyFactory
                    .getInstance(ALGORITHM);
            return keyfactory.generateSecret(deSedeKeySpec);
        }

        HLogF.d(TAG, "DES方式");
        DESKeySpec dks = new DESKeySpec(key.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        return keyFactory.generateSecret(dks);
    }

    /**
     * DES算法，加密
     *
     * @param data 待加密字符串
     * @param key  加密私钥，长度不能够小于8位
     * @return 加密后的字节数组，一般结合Base64编码使用
     */
    public static String encode(String key, String data) {
        if (null == data) {
            data = "";
        }
        return encode(key, data.getBytes());
    }


    /**
     * DES算法，加密
     *
     * @param data 待加密字符串
     * @param key  加密私钥，长度不能够小于8位
     * @return 加密后的字节数组，一般结合Base64编码使用
     */
    public static String encode(String key, byte[] data) {
        HLogF.d(TAG, "加密");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM + WORK_MODE + MODE_TIANCHONG);
            if (WORK_MODE.indexOf("ECB") != -1) {
                HLogF.d(TAG, "ECB模式 " + ALGORITHM + WORK_MODE + MODE_TIANCHONG);
                cipher.init(Cipher.ENCRYPT_MODE, getRawKey(key));
            } else {
                HLogF.d(TAG, "偏移模式 " + ALGORITHM + WORK_MODE + MODE_TIANCHONG);
                IvParameterSpec iv = new IvParameterSpec(IVPARAMETERSPEC.getBytes());
                cipher.init(Cipher.ENCRYPT_MODE, getRawKey(key), iv);
            }
            byte[] bytes = cipher.doFinal(data);
            return Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取编码后的值
     *
     * @param key
     * @param data
     * @return
     */
    public static String decode(String key, String data) {
        if (null == data) {
            data = "";
        }
        return decode(key, Base64.decode(data, Base64.DEFAULT));
    }

    /**
     * DES算法，解密
     *
     * @param data 待解密字符串
     * @param key  解密私钥，长度不能够小于8位
     * @return 解密后的字节数组
     */
    public static String decode(String key, byte[] data) {
        HLogF.d(TAG, "解密");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM + WORK_MODE + MODE_TIANCHONG);
            if (WORK_MODE.indexOf("ECB") != -1) {
                HLogF.d(TAG, "ECB模式 " + ALGORITHM + WORK_MODE + MODE_TIANCHONG);
                cipher.init(Cipher.DECRYPT_MODE, getRawKey(key));
            } else {
                HLogF.d(TAG, "偏移模式 " + ALGORITHM + WORK_MODE + MODE_TIANCHONG);
                IvParameterSpec iv = new IvParameterSpec(IVPARAMETERSPEC.getBytes());
                cipher.init(Cipher.DECRYPT_MODE, getRawKey(key), iv);
            }
            byte[] original = cipher.doFinal(data);
            String originalString = new String(original);
            return originalString;
        } catch (Exception e) {
            return null;
        }
    }
} 