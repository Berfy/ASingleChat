package cn.berfy.service.im.util;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.berfy.sdk.mvpbase.base.BaseApplication;
import cn.berfy.service.im.R;


/**
 * 时间转换工具
 */
public class IMTimeUtil {
    private static final int HM = 0, HMMD = 1,MDHM = 2,YMD = 3, YMDHM = 4;

    private IMTimeUtil() {

    }

    /**
     * 时间转化为显示字符串
     *
     * @param timeStamp 单位为秒
     */
    public static String getTimeStr(long timeStamp) {
        if (timeStamp == 0) return "";
        Calendar inputTime = Calendar.getInstance();
        inputTime.setTimeInMillis(timeStamp * 1000);
        Date currentTimeZone = inputTime.getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        if (calendar.before(inputTime)) {
            //今天23:59在输入时间之前，解决一些时间误差，把当天时间显示到这里
            SimpleDateFormat sdf = getSimpleDate(YMD);
            return sdf.format(currentTimeZone);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.before(inputTime)) {
            SimpleDateFormat sdf = getSimpleDate(HM);
            return sdf.format(currentTimeZone);
            //消息界面中的今天的信息 不显示 今天 俩字
            //            return sdf.format(currentTimeZone) + " 今天";
        }
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        if (calendar.before(inputTime)) {
            SimpleDateFormat sdf = getSimpleDate(HM);
            return sdf.format(currentTimeZone) + " "
                    + BaseApplication.getContext().getResources().getString(R.string.im_time_yesterday);
        } else {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            if (calendar.before(inputTime)) {
                SimpleDateFormat sdf = getSimpleDate(HMMD);
                return sdf.format(currentTimeZone);
            } else {
                SimpleDateFormat sdf = getSimpleDate(YMD);
                return sdf.format(currentTimeZone);
            }
        }
    }

    /**
     * 时间转化为聊天界面显示字符串
     *
     * @param timeStamp 单位为秒
     */
    public static String getChatTimeStr(long timeStamp) {
        if (timeStamp == 0) return "";
        Calendar inputTime = Calendar.getInstance();
        inputTime.setTimeInMillis(timeStamp * 1000);
        Date currenTimeZone = inputTime.getTime();
        Calendar calendar = Calendar.getInstance();
        if (!calendar.after(inputTime)) {
            //当前时间在输入时间之前
            SimpleDateFormat sdf = getSimpleDate(YMD);
            return sdf.format(currenTimeZone);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.before(inputTime)) {
            SimpleDateFormat sdf = getSimpleDate(HM);
            return sdf.format(currenTimeZone);
        }
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        if (calendar.before(inputTime)) {
            SimpleDateFormat sdf = getSimpleDate(HM);
            return BaseApplication.getContext().getResources().getString(R.string.im_time_yesterday) + " " + sdf.format(currenTimeZone);
        } else {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.MONTH, Calendar.JANUARY);
            if (calendar.before(inputTime)) {
                SimpleDateFormat sdf = getSimpleDate(MDHM);
                return sdf.format(currenTimeZone);
            } else {
                SimpleDateFormat sdf = getSimpleDate(YMDHM);
                return sdf.format(currenTimeZone);
            }

        }
    }

    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat getSimpleDate(int dateFormat) {
        SimpleDateFormat sdf = null;
        switch (dateFormat) {
            case HM:
                sdf = new SimpleDateFormat("HH:mm");
                break;
            case HMMD:
                sdf = new SimpleDateFormat("HH:mm M" + BaseApplication.getContext().getResources().getString(R.string.im_time_month) + "d" + BaseApplication.getContext().getResources().getString(R.string.im_time_day));
                break;
            case MDHM:
                sdf = new SimpleDateFormat("M" + BaseApplication.getContext().getResources().getString(R.string.im_time_month) + "d" + BaseApplication.getContext().getResources().getString(R.string.im_time_day) + " HH:mm");
                break;
            case YMD:
                sdf = new SimpleDateFormat("yyyy" + BaseApplication.getContext().getResources().getString(R.string.im_time_year) + "MM" + BaseApplication.getContext().getResources().getString(R.string.im_time_month) + "dd" + BaseApplication.getContext().getResources().getString(R.string.im_time_day));
                break;
            case YMDHM:
                sdf = new SimpleDateFormat("yyyy" + BaseApplication.getContext().getResources().getString(R.string.im_time_year) + "MM" + BaseApplication.getContext().getResources().getString(R.string.im_time_month) + "dd" + BaseApplication.getContext().getResources().getString(R.string.im_time_day) + " HH:mm");
                break;

        }
        return sdf;
    }
}
