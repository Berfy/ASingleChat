package cn.berfy.sdk.mvpbase.util;

/**
 * Created by ycz on 2018/1/25.
 * use:
 */

public class NickNameUtil {
    public static String getNickName(String nickName) {
//        String name = p.getName();
        if (nickName.length() > 6) {
            nickName = nickName.substring(0, 6);
            return String.format("%s...", nickName);
        } else {
            return nickName + "";
        }
    }

    ;
}
