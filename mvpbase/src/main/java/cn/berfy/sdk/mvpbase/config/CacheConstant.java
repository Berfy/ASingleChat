package cn.berfy.sdk.mvpbase.config;

import android.os.Environment;

import java.io.File;

/**
 * Created by Berfy on 2017/12/19.
 * 缓存相关配置
 */
public class CacheConstant {

    /**
     * 文件目录路径
     */
    public static String DIR_PUBLIC_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();

    /**
     * @param rootDirName 目录名
     */
    public static void setRootDir(String rootDirName) {
        DIR_PUBLIC_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + rootDirName;
        AD_FILE_DIR = DIR_PUBLIC_ROOT + "/AdInfo";
        MEDIA_FILE_DIR = DIR_PUBLIC_ROOT + "/Media";
        VOICE_FILE_DIR = DIR_PUBLIC_ROOT + "/AAC";
        MultiMedia_FILE_DIR = DIR_PUBLIC_ROOT + "/MultiMedia";
        PICTURE_FILE_DIR = DIR_PUBLIC_ROOT + "/Picture";
        CACHE_FILE_DIR = DIR_PUBLIC_ROOT + "/cache";
        CRASH_FILE_DIR = DIR_PUBLIC_ROOT + "/crash";
        VIDEO_STORAGE_DIR = DIR_PUBLIC_ROOT + "/ShortVideo";
        MEDIA_TRIM_FILE_DIR = MEDIA_FILE_DIR + "/trim";
        MEDIA_COMPRESSED_FILE_DIR = MEDIA_FILE_DIR + "/compressed";
        PICTURE_CHILD_FILE_DIR = MEDIA_COMPRESSED_FILE_DIR + "/VideoBlurPicture";
        VIDEO_TS = MEDIA_COMPRESSED_FILE_DIR + "/videoTs";
        COMPRESS_PATH = MEDIA_COMPRESSED_FILE_DIR + File.separator + "compressed_video_clip.mp4";//最终合成视频路径
        WATER_MARK_PATH = PICTURE_FILE_DIR + File.separator + "WaterMarkVideo.mp4";//添加原视频路径
    }

    /**
     * SharedPreference文件名
     */
    public static String XML_FILENAME = "zxMqtTSdk";
    public static String XML_FILENAME_PUBLIC = "lottery_public";//和用户登录无关
    public static final String XML_USER_DATA = "xml_user_data";
    public static final String XML_NOT_NOTIFY_DATA = "xml_notNotify_data";//免打扰数据

    //广告图片、视频目录
    public static String AD_FILE_DIR = DIR_PUBLIC_ROOT + "/AdInfo";

    //视频文件路径
    public static String MEDIA_FILE_DIR = DIR_PUBLIC_ROOT + "/Media";

    //图片缓存文件路径
    public static String CACHE_FILE_DIR = DIR_PUBLIC_ROOT + "/cache";

    //错误Log文件路径
    public static String CRASH_FILE_DIR = DIR_PUBLIC_ROOT + "/crash";

    public static String VOICE_FILE_DIR = DIR_PUBLIC_ROOT + "/AAC";
    //多媒体文件
    public static String MultiMedia_FILE_DIR = DIR_PUBLIC_ROOT + "/MultiMedia";
    //图片
    public static String PICTURE_FILE_DIR = DIR_PUBLIC_ROOT + "/Picture";

    //文件
    public static String FILE_DIR = DIR_PUBLIC_ROOT + "/File";

    public static String VIDEO_STORAGE_DIR = DIR_PUBLIC_ROOT + "/ShortVideo";
    //本地视频截取路径
    public static String MEDIA_TRIM_FILE_DIR = MEDIA_FILE_DIR + "/trim";
    //本地视频压缩路径
    public static String MEDIA_COMPRESSED_FILE_DIR = MEDIA_FILE_DIR + "/compressed";
    //处理视频
    public static String PICTURE_CHILD_FILE_DIR = MEDIA_COMPRESSED_FILE_DIR + "/VideoBlurPicture";
    public static String VIDEO_TS = MEDIA_COMPRESSED_FILE_DIR + "/videoTs";
    public static String COMPRESS_PATH = MEDIA_COMPRESSED_FILE_DIR + File.separator + "compressed_video_clip.mp4";//最终合成视频路径
    public static String WATER_MARK_PATH = PICTURE_FILE_DIR + File.separator + "WaterMarkVideo.mp4";//添加原视频路径


    public static final String CURRENT_LOGIN_USER = "CURRENT_LOGIN_USER";//用户缓存

    public static final String XML_APK_UPDATE_URL = "apk_update_url";//下载更新Apk的路径

    /*SharedPreference存储字段*/
    public static final String XML_MESSAGE = "messageEvent";

    /*私信未读消息数量缓存*/
    public static final String XML_MESSAGE_UNREAD_NUM = "messageUnReadNum";

    /*单独记录的未读消息数  系统消息有消息就是1没有就是0  私信消息具体数字IM返回*/
    public static final String XML_MESSAGE_UNREAD_NUM_FOR_MIUI = "messageUnReadNumForMiUI";

    public static final String CURRENT_LOGIN_USER_INFO = "CURRENT_LOGIN_USER_INFO";//用户userInfo缓存

    public static final String XML_ISCALLING = "isCalling";//是否通话中

    public static final String XML_CONFIG = "ConfigData";
    //主题
    public static final String XML_THEME = "theme";
    public static final String XML_THEME_NEWYEAR = "theme_newyear";

    //签到提示框不再提示缓存
    public static final String XML_SIGN_IN_NOT_SEE = "sign_in_not_see_id=";

    public static final String APP_RUNTIME_CONFIG = "APP_RUNTIME_CONFIG";//客户端运行时配置缓存KEY

    public static final String HOME_CHICKEN_GUIDE = "home_chicken_guide";//首页吃鸡引导
    public static final String HOME_FLEET_GUIDE = "home_fleet_guide";//首页车队引导
    public static final String MESSAGE_CREATE_CHATROOM_GUIDE = "message_create_chatroom_guide";//消息创建群组引导
    public static final String ORDER_DATA_MAP = "order_data_map";//收到的接单信息
    public static final String ORDER_LIVE_IS_CHECKED = "order_live_is_checked";//开车时是否勾选了"直播车队"
    //是否已经展示过功能引导页 主播开车等候区页面
    public static final String IS_SHOW_WAITING_GUIDE = "IS_SHOW_WAITING_GUIDE";

    public static final String ORDER_MAKE_CAR_TYPE = "order_make_type";//开车时开车类型
    public static final String ORDER_MAKE_CAR_LABEL_NAME = "order_make_label_name";//开车时开车标签名称
    public static final String ORDER_MAKE_CAR_LABEL_ID = "order_make_label_id";//开车时开车标签id
    public static final String ORDER_MAKE_CAR_TIME = "order_make_time";//开车时开车时间
    public static final String ORDER_MAKE_CAR_DEST = "order_make_dest";//开车时开车描述

}
