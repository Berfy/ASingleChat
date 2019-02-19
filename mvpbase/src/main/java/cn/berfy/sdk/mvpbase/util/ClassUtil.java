package cn.berfy.sdk.mvpbase.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Berfy
 * 类处理
 */
public class ClassUtil {

    /**
     * @author Berfy
     * 判断两个实体类数据是否一样
     */
    public static <T> boolean compare(T obj1, T Obj2)
            throws Exception {
        Field[] fs = obj1.getClass().getDeclaredFields();//获取所有属性
        boolean isSame = true;
        for (Field f : fs) {
            f.setAccessible(true);//设置访问性，反射类的方法，设置为true就可以访问private修饰的东西，否则无法访问
            Object v1 = f.get(obj1);
            Object v2 = f.get(Obj2);
            LogF.d("ClassUtil", "对比类成员" + f.getName() + " " + equals(v1, v2));
            if (!equals(v1, v2)) {//对象不同  对象是实体类递归查询
                LogF.d("ClassUtil", "不同" + GsonUtil.getInstance().toJson(v1) + "  " + GsonUtil.getInstance().toJson(v2));
                if (null != v1 && null != v2) {
                    Field[] fs1 = v1.getClass().getDeclaredFields();//获取所有属性
                    if (fs1.length > 0) {
                        LogF.d("ClassUtil", "对比类成员 深度对比");
                        isSame = compare(v1, v2);
                    } else {
                        isSame = false;
                    }
                } else {
                    isSame = false;
                }
            }
        }
        return isSame;
    }

    /**
     * @author Berfy
     * 判断两个实体类数据是否一样
     */
    public static <T> Map<String, String> compareResult(T obj1, T Obj2)
            throws Exception {
        Map<String, String> result = new HashMap<String, String>();
        Field[] fs = obj1.getClass().getDeclaredFields();//获取所有属性
        for (Field f : fs) {
            f.setAccessible(true);//设置访问性，反射类的方法，设置为true就可以访问private修饰的东西，否则无法访问
            Object v1 = f.get(obj1);
            Object v2 = f.get(Obj2);
            result.put(f.getName(), String.valueOf(equals(v1, v2)));
        }
        return result;
    }

    public static boolean equals(Object obj1, Object obj2) {

        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }

}
