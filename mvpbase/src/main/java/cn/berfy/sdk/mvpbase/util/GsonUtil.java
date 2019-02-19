package cn.berfy.sdk.mvpbase.util;

import com.google.gson.Gson;
import java.lang.reflect.Type;

/**
 * @author Berfy
 * Json解析器
 */
public class GsonUtil {

    private static GsonUtil mGsonUtil;
    private Gson mGson;

    synchronized public static GsonUtil getInstance() {
        if (null == mGsonUtil) {
            synchronized (GsonUtil.class) {
                if (null == mGsonUtil) {
                    mGsonUtil = new GsonUtil();
                }
            }
        }
        return mGsonUtil;
    }

    private GsonUtil() {
        mGson = new Gson();
    }

    public <T> String toJson(T classType) {
        return mGson.toJson(classType);
    }

    public <T> T toClass(String json, Class<T> classname) {
        return mGson.fromJson(json, classname);
    }

    public <T> T toClass(String json, Type type) {
        return mGson.fromJson(json, type);
    }
}
