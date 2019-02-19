package cn.berfy.sdk.mvpbase.util;

/**
 * HOST管理
 * Created by Berfy on 2016-08-10.
 */
public class HostSettings {

    //设置默认服务器地址
    private static int HOST_TYPE = HostSettings.HOST_TYPE_DEV;

    /*
     * 服务器地址---开发
     */
    private static final String HOST_DEV = "http://223.203.221.89:9090/";
    private static final String HOST_MQTT_DEV = "tcp://223.203.221.89:8908";

    /*;
     * 服务器地址---测试
     */
    private static final String HOST_TEST = "http://223.203.221.89:9090";
    private static final String HOST_MQTT_TEST = "tcp://223.203.221.89:8908";

    /*
     * 服务器地址---封测准正式服
     */
    private static final String HOST_PREPARED = "";
    private static final String HOST_MQTT_PREPARED = "";

    /*
     * 服务器地址--正式
     */
    private static final String HOST_PRO = "";
    private static final String HOST_MQTT_PRO = "";

    public final static int HOST_TYPE_DEV = 0;
    public final static int HOST_TYPE_TEST = 1;
    public final static int HOST_TYPE_PREPARED = 2;
    public final static int HOST_TYPE_PRO = 3;

    public static String getHost() {
        String host = HostSettings.HOST_TEST;
        switch (HOST_TYPE) {
            case HostSettings.HOST_TYPE_DEV:
                host = HOST_DEV;
                break;
            case HostSettings.HOST_TYPE_TEST:
                host = HOST_TEST;
                break;
            case HostSettings.HOST_TYPE_PREPARED:
                host = HOST_PREPARED;
                break;
            case HostSettings.HOST_TYPE_PRO:
                host = HOST_PRO;
                break;
        }
        return host;
    }

    public static String getMQTTHost() {
        String host = HostSettings.HOST_MQTT_DEV;
        switch (HOST_TYPE) {
            case HostSettings.HOST_TYPE_DEV:
                host = HOST_MQTT_DEV;
                break;
            case HostSettings.HOST_TYPE_TEST:
                host = HOST_MQTT_TEST;
                break;
            case HostSettings.HOST_TYPE_PREPARED:
                host = HOST_MQTT_PREPARED;
                break;
            case HostSettings.HOST_TYPE_PRO:
                host = HOST_MQTT_PRO;
                break;
        }
        return host;
    }

    public static int getHostType() {
        return HOST_TYPE;
    }

    public static void setHostType(int type) {
        HOST_TYPE = type;
    }
}
