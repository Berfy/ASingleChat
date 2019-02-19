package cn.berfy.sdk.mvpbase.util;

import android.annotation.SuppressLint;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {

    /**
     * 获取每个月最后一天
     */
    public static int getLastDayOfMonth(int year, int month) {
        if (month == 0) {
            month = 12;
        }
        if (month == 13) {
            month = 1;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);// 设置为下个月
        cal.set(Calendar.DATE, 1); // 设置为该月第一天
        cal.add(Calendar.DATE, -1); // 再减一天即为上个月(这个月)最后一天
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 判断前后年份
     *
     * @param isPreMonth true判断上个月年份 false判断下个月年份
     */
    public static int getTrueYear(int year, int month, boolean isPreMonth) {
        if (isPreMonth) {
            if (month <= 1) {
                year--;
            }
        } else {
            if (month >= 12) {
                year++;
            }
        }
        return year;
    }

    /**
     * 判断前后月份
     *
     * @param isPreMonth true判断上个月 false判断下个月
     */
    public static int getTrueMonth(int month, boolean isPreMonth) {
        if (isPreMonth) {
            month--;
            if (month < 1) {
                month = 12;
            }
        } else {
            month++;
            if (month > 12) {
                month = 1;
            }
        }
        return month;
    }

    public static String getCurrentDate() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        return year + "-" + (month < 10 ? "0" + month : month) + "-"
                + (day < 10 ? "0" + day : day);
    }

    public static String getYesterDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1;
        return year + "-" + (month < 10 ? "0" + month : month) + "-"
                + (day < 10 ? "0" + day : day);
    }

    public static String getCurrentTime() {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        int second = Calendar.getInstance().get(Calendar.SECOND);
        return year + "-" + (month < 10 ? "0" + month : month) + "-"
                + (day < 10 ? "0" + day : day) + " " + (hour < 10 ? "0" + hour : hour) + ":" + (minute < 10 ? "0" + minute : minute) + ":" + (second < 10 ? "0" + second : second);
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getCurrentTime(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String time = formatter.format(curDate);
        return time;
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getCurrentTimeCHYear() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String time = formatter.format(curDate);
        return time;
    }

    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static int getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    public static int getCurrentDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 判断时间是不是今天
     *
     * @param time
     * @return 是返回true，不是返回false
     */
    @SuppressLint("SimpleDateFormat")
    public static boolean isNow(long time) {
        Date date = new Date(time);
        //当前时间
        Date now = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        //获取今天的日期
        String nowDay = sf.format(now);
        LogF.d("判断今天", "今天 " + nowDay);
        //对比的时间
        String day = sf.format(date);
        LogF.d("判断今天", "对比日期 " + day);
        return day.equals(nowDay);
    }

    @SuppressLint("SimpleDateFormat")
    public static boolean isSameDay(long lastTime,long thisTime) {
        Date date = new Date(lastTime);
        //当前时间
        Date now = new Date(thisTime);
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        //获取今天的日期
        String nowDay = sf.format(now);
        LogF.d("判断今天", "今天 " + nowDay);
        //对比的时间
        String day = sf.format(date);
        LogF.d("判断今天", "对比日期 " + day);
        return day.equals(nowDay);
    }

    public static boolean isSameYear(long itemTime) {
        Date now = new Date(System.currentTimeMillis());//当前时间
        Date date = new Date(itemTime); //对比的时间
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(now);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    //是否同天,是否同年,是否同月
    private static boolean isSameDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        boolean isSameYear = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        boolean isSameMonth = isSameYear
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean isSameDate = isSameMonth
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
        return isSameDate;
    }



    public static boolean isYesterday(long timestamp) {
        Calendar c = Calendar.getInstance();
        clearCalendar(c, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND);
        c.add(Calendar.DAY_OF_MONTH, -1);
        long firstOfDay = c.getTimeInMillis(); // 昨天最早时间
        c.setTimeInMillis(timestamp);
        clearCalendar(c, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND); // 指定时间戳当天最早时间
        return firstOfDay == c.getTimeInMillis();
    }

    private static void clearCalendar(Calendar c, int... fields) {
        for (int f : fields) {
            c.set(f, 0);
        }
    }

    /**
     * 获取两个日期之间的间隔天数
     *
     * @return
     */
    public static int getGapCount(Date startDate, Date endDate) {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(startDate);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(endDate);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        return (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime()
                .getTime()) / (1000 * 60 * 60 * 24));
    }

    public static String timeFormat(long timeMillis, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        return format.format(new Date(timeMillis));
    }

    public static long timeFormat(String dateTime, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        try {
            return format.parse(dateTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String formatPhotoDate(long time) {
        return timeFormat(time, "yyyy-MM-dd");
    }

    public static String formatPhotoDate(String path) {
        File file = new File(path);
        if (file.exists()) {
            long time = file.lastModified();
            return formatPhotoDate(time);
        }
        return "1970-01-01";
    }

    /**
     * 把服务器返回的时间戳转换成日期字符串
     */
    public static String format(String pattern, long timeMillis) {
        if (timeMillis == 0) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        return format.format(new Date(timeMillis));
    }

    /**
     * 把服务器返回的时间戳转换成日期字符串
     */
    public static String timestampConvertDate(String dataFormat, long timeStamp) {
        if (timeStamp == 0) return "";
        SimpleDateFormat format = new SimpleDateFormat(dataFormat, Locale.CHINA);
        return format.format(new Date(timeStamp));
    }

    /**
     * 计算睡眠时长
     */
    public static String getSleepTime(String sleepTime, String wakeUpTime) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date d1 = df.parse("2015-09-25 " + wakeUpTime + ":00");
            Date d2 = df.parse("2015-09-24 " + sleepTime + ":00");// 计算昨天睡眠和今日起床时间差
            long diff = d1.getTime() - d2.getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            long hours = (diff - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
            long minutes = (diff - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60)) / (1000 * 60);
            LogF.e("睡眠时间", "睡眠时间" + hours + "  " + minutes + "小数：" + (minutes / 60.00));
            String sleepStr = (hours + minutes / 60.00) + "";
            if ((minutes / 60.00) == 0) {
                return hours + "";
            } else {
                return sleepStr;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    /**
     * 格式化时间
     * 显示SMS聊天格式
     */
    public static String formatTimeToSMS(long timeStamp) {
        LogF.d("转换前时间", timeFormat(timeStamp, "yyyy-MM-dd HH:mm:ss"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
        long curTimeMillis = System.currentTimeMillis();
        int todayHoursSeconds = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60;
        int todayMinutesSeconds = calendar.get(Calendar.MINUTE) * 60;
        int todaySeconds = calendar.get(Calendar.SECOND);
        int todayMillis = (todayHoursSeconds + todayMinutesSeconds + todaySeconds) * 1000;
        long todayStartMillis = curTimeMillis - todayMillis;

        LogF.d("今天开始时间", timeFormat(todayStartMillis, "yyyy-MM-dd HH:mm:ss"));
//        Date date = new Date(curTimeMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        if (timeStamp >= todayStartMillis) {
            return "今天 " + sdf.format(timeStamp);
        }
        int oneDayMillis = 24 * 60 * 60 * 1000;
        long yesterdayStartMilis = todayStartMillis - oneDayMillis;
        if (timeStamp >= yesterdayStartMilis) {
            return "昨天 " + sdf.format(timeStamp);
        }
        long yesterdayBeforeStartMilis = yesterdayStartMilis - oneDayMillis;
        if (timeStamp >= yesterdayBeforeStartMilis) {
            return "前天 " + sdf.format(timeStamp);
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(timeStamp);
    }

    /**
     * @param timeStamp 毫秒
     * @return
     */
    public static String getFormatTime(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        long curTimeMillis = System.currentTimeMillis();
        int todayHoursSeconds = calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60;
        int todayMinutesSeconds = calendar.get(Calendar.MINUTE) * 60;
        int todaySeconds = calendar.get(Calendar.SECOND);
        int todayMillis = (todayHoursSeconds + todayMinutesSeconds + todaySeconds) * 1000;
        long todayStartMillis = curTimeMillis - todayMillis;

//        Date date = new Date(curTimeMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        if (timeStamp >= todayStartMillis) {
            return "今天 " + sdf.format(timeStamp);
        }
        int oneDayMillis = 24 * 60 * 60 * 1000;
        long yesterdayStartMilis = todayStartMillis - oneDayMillis;
        if (timeStamp >= yesterdayStartMilis) {
            return "昨天 " ;
        }
        long yesterdayBeforeStartMilis = yesterdayStartMilis - oneDayMillis;
        if (timeStamp >= yesterdayBeforeStartMilis) {
            return "前天 ";
        }
        return new SimpleDateFormat("MM-dd ").format(timeStamp);
    }

    //判断是否可以包夜
    public static boolean canNight() {
        return isBelong("23:00:00", "24:00:00") ||
                isBelong("00:00:00", "07:00:00");
    }

    public static boolean isBelong(String startTimeStr, String endTimeStr) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");//设置日期格式
        Date now = null;
        Date beginTime = null;
        Date endTime = null;
        try {
            now = df.parse(df.format(new Date()));
            beginTime = df.parse(startTimeStr);
            endTime = df.parse(endTimeStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return belongCalendar(now, beginTime, endTime);
    }

    /**
     * 判断时间是否在时间段内
     *
     * @param nowTime
     * @param beginTime
     * @param endTime
     * @return
     */
    public static boolean belongCalendar(Date nowTime, Date beginTime, Date endTime) {
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getGameHour(String min) {
        try {
            if (min.equals("481")) {
                return "包夜";
            } else return Integer.parseInt(min) / 60 + "小时";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    /**
     * 获取时间格式,将s变为 mm:ss
     * times 秒
     */
    public static String formatMinTime(long second) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");//初始化Formatter的转换格式。
        String hms = formatter.format(second);
        return hms;
    }

    /**
     * 获取时间格式,将s变为 hh:mm:ss
     * times 秒
     */
    public static String formatTime(long second) {
        long hours = second / 3600;
        long minutes = (second % 3600) / 60;
        long seconds = second % 60;

        String hourString = "";
        String minuteString = "";
        String secondString = "";
        if (hours < 10) {
            hourString = "0";
            if (hours == 0) {
                hourString += "0";
            } else {
                hourString += String.valueOf(hours);
            }
        } else {
            hourString = String.valueOf(hours);
        }
        if (minutes < 10) {
            minuteString = "0";
            if (minutes == 0) {
                minuteString += "0";
            } else {
                minuteString += String.valueOf(minutes);
            }
        } else {
            minuteString = String.valueOf(minutes);
        }
        if (seconds < 10) {
            secondString = "0";
            if (seconds == 0) {
                secondString += "0";
            } else {
                secondString += String.valueOf(seconds);
            }
        } else {
            secondString = String.valueOf(seconds);
        }
        return hourString + ":" + minuteString + ":" + secondString;
    }
}
