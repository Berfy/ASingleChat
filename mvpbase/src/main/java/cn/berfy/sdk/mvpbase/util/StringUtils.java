package cn.berfy.sdk.mvpbase.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.CheckBox;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.base.BaseApplication;

import static java.lang.String.format;

/**
 * 字符串相关的操作
 * Created by Rothschild on 2016-08-23.
 */
public class StringUtils {

    /**
     * 判断是否是手机号
     *
     * @param phone 手机号
     * @return boolean
     */
    public static boolean checkPhone(String phone) {
        //        Pattern pattern = Pattern
        //                .compile("^(13[0-9]|15[0-9]|153|15[6-9]|180|18[23]|18[5-9])\\d{8}$");
        Pattern pattern = Pattern
                .compile("^(1(([35][0-9])|[8][0-9]|[7][0-9]|[4][0-9]))\\d{8}$");
        Matcher matcher = pattern.matcher(phone);

        return matcher.matches();
    }

    /**
     * @param name 昵称
     * @return boolean
     * ^[a-zA-Z][a-zA-Z0-9_-]{0,15}$
     */
    public static boolean checkNickName(String name) {
        Pattern pattern = Pattern
                .compile("^[\\u4e00-\\u9fa5_a-zA-Z0-9-]{1,16}$");
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    /**
     * 描述：是否是邮箱.
     *
     * @param str 指定的字符串
     * @return 是否是邮箱:是为true，否则false
     */
    public static Boolean isEmail(String str) {
        Boolean isEmail = false;
        String expr = "^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w{2,3}){1,3})$";
        if (str.matches(expr)) {
            isEmail = true;
        }
        return isEmail;
    }

    public static boolean isIdCard(String idCard) {
        boolean isIdCard = false;
        if (TextUtils.isEmpty(idCard)) {
            return false;
        }
        int length = idCard.length();
        String expr = "";
        if (length == 15) {
            expr = "^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{2}$";
        } else if (length == 18) {
            expr = "^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
        }
        if (idCard.matches(expr)) {
            isIdCard = true;
        }
        return isIdCard;
    }

    /**
     * 将大于10000的字符串变为1w形式,保留2位小数且不四舍五入
     */
    public static String getNum(String hotDegree) {
        if (TextUtils.isEmpty(hotDegree))
            return "0";
        double temp = Double.valueOf(hotDegree) / 10000;
        if (temp >= 1) {
            hotDegree = format("%.2f", temp - 0.005);
            if (hotDegree.contains(".00")) {
                hotDegree = hotDegree.replace(".00", "");
            } else if (hotDegree.contains(".") && hotDegree.charAt(hotDegree.length() - 1) == '0') {
                hotDegree = hotDegree.replace(String.valueOf(hotDegree.charAt(hotDegree.length() - 1)), "");
            }
            hotDegree = format(Locale.CHINA, "%s万", hotDegree);
        }
        return hotDegree;
    }

    public static String getNum(String hotDegree, String unit) {
        if (TextUtils.isEmpty(hotDegree))
            return "0";
        double temp = Double.valueOf(hotDegree) / 10000;
        if (temp >= 1) {
            hotDegree = format("%.2f", temp - 0.005);
            if (hotDegree.contains(".00")) {
                hotDegree = hotDegree.replace(".00", "");
            } else if (hotDegree.contains(".") && hotDegree.charAt(hotDegree.length() - 1) == '0') {
                hotDegree = hotDegree.replace(String.valueOf(hotDegree.charAt(hotDegree.length() - 1)), "");
            }
            hotDegree = format(Locale.CHINA, "%s%s", hotDegree, unit);
        }
        return hotDegree;
    }

    /**
     * 将将m转化成km形式,保留2位小数
     */
    public static String getKilometer(String meter, String nullMeterTip) {
        if (!TextUtils.isEmpty(meter) && !"0".equals(meter)) {
            double temp = Double.valueOf(meter) / 1000;
            meter = String.format(Locale.CHINA, "%.2f km", temp);
            return meter;
        } else {
            return nullMeterTip;
        }
    }

    //根据出生日期获取年龄
    public static int getAge(Date birthDay) {
        Calendar cal = Calendar.getInstance();

        if (cal.before(birthDay)) {
            ToastUtil.getInstances().showShort("年龄应在12到89岁之间，请重新设置!");
        }
        int yearNow = cal.get(Calendar.YEAR);
        int monthNow = cal.get(Calendar.MONTH);
        int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
        cal.setTime(birthDay);

        int yearBirth = cal.get(Calendar.YEAR);
        int monthBirth = cal.get(Calendar.MONTH);
        int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

        int age = yearNow - yearBirth;
        if (monthNow <= monthBirth) {
            if (monthNow == monthBirth) {
                if (dayOfMonthNow < dayOfMonthBirth)
                    age--;
            } else {
                age--;
            }
        }
        return age;
    }

    //将时间戳转换成字符串
    public static String timeStampToString(long time, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
            return sdf.format(new Date(time));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //将出生日期转换为unix时间戳
    public static String date2TimeStamp(String dateStr, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(dateStr).getTime() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String stamp2Date(String stamp) {
        String d = "";
        try {
            long l = Long.valueOf(stamp);
            Date date = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.getDefault());
            d = format.format(date);
            return d;
        } catch (Exception e) {
            e.printStackTrace();
            return d;
        }


    }


    //日期转换
    public static Calendar str2Calender(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date date = null;
        try {
            date = sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }


    /**
     * @param s 要转换的秒数
     * @return @return 该毫秒数转换为 days |hours|minutes 的格式
     */
    public static String formatDuring(int s) {
        String result = "";
        int days = s / (60 * 60 * 24);
        int hours = (s % (60 * 60 * 24)) / (60 * 60);
        int minutes = (s % (60 * 60)) / (60);
        if (days > 0) {
            result = String.format(Locale.CHINA, "%d天", days);
        } else if (hours > 0) {
            result = String.format(Locale.CHINA, "%d小时", hours);
        } else if (minutes > 0) {
            result = String.format(Locale.CHINA, "%d分钟", minutes);
        }
        return result;
    }

    /**
     * 转换毫秒数成“分、秒”，如“01:53”,显示“时、分、秒”，如“01:01:30
     */
    public static String getTimeFormat(long time) {
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;

        long hour = (time) / hh;
        long minute = (time - hour * hh) / mi;
        long second = (time - hour * hh - minute * mi) / ss;

        String strHour = hour < 10 ? "0" + hour : "" + hour;
        String strMinute = minute < 10 ? "0" + minute : "" + minute;
        String strSecond = second < 10 ? "0" + second : "" + second;
        return strHour + ":" + strMinute + ":" + strSecond;
    }


    /**
     * 转换毫秒数成“分、秒”，如“01:53”,显示“时、分、秒”，如“01:01:30
     */
    public static String getChatTimeFormat(long time) {
        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        StringBuilder sb = new StringBuilder();
        long hour = (time) / hh;
        long minute = (time - hour * hh) / mi;
        long second = (time - hour * hh - minute * mi) / ss;

        if (hour > 0) {
            sb.append(hour + "小时");
        }
        if (minute > 0) {
            sb.append(minute + "分");
        }
        if (second > 0) {
            sb.append(second + "秒");
        }
        return sb.toString();
    }


    //获取筛选数据
    public static String getFilterParam(String data, String defaultData) {
        return TextUtils.isEmpty(data) ? defaultData : data;
    }

    public static String formToyuan(String fen) {
        int yuan = Integer.valueOf(fen);
        double v = Math.round(yuan) / 100.0;
        return "" + v;
    }

    /**
     * 获取文本宽度
     *
     * @param text
     * @param textSize
     * @return
     */
    public static float getTextWidth(String text, float textSize) {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textSize);
        return textPaint.measureText(text);
    }

    /**
     * 对文本做宽度限制
     *
     * @param text
     * @param ems
     * @param textsize
     * @return
     */
    public static String limitTextWith(String text, float ems, float textsize) {

        String ss = text;
        while (getTextWidth(ss, textsize) > ems) {
            ss = ss.subSequence(0, ss.length() - 1).toString();
        }
        if (!ss.equals(text)) {
            ss += "...";
        }
        return ss;
    }

    /**
     * 返回指定宽度的字符串
     *
     * @param text
     * @param ems
     * @param textSize
     * @return
     */
    public static String getTextLimitWith(String text, float ems, float textSize) {
        String ss = text;
        while (getTextWidth(ss, textSize) > ems) {
            ss = ss.subSequence(0, ss.length() - 1).toString();
        }
        return ss;
    }

    public static boolean limitWith(String text, float ems, float textSize) {
        return getTextWidth(text, textSize) <= ems;
    }

    public static String getInfo(String content) {
        return TextUtils.isEmpty(content) ? "" : content;
    }

    /**
     * 将分钟数转换成带有以为小数的小时数
     *
     * @param formatString
     * @param minutes
     * @return
     */
    public static String minuteToHour(String formatString, double minutes) {
        double temp = minutes / 60;
        return String.format(Locale.CHINA, formatString, temp);
    }

    /**
     * 将大于10000的字符串变为1w形式,保留2位小数且不四舍五入
     */
    public static String getNumFloor(String hotDegree) {
        if (TextUtils.isEmpty(hotDegree))
            return "0";
        double temp = Double.valueOf(hotDegree) / 10000;
        if (temp >= 1) {
            hotDegree = format("%.1f", temp - 0.05);
            if (hotDegree.contains(".0")) {
                hotDegree = hotDegree.replace(".0", "");
            }
            //            hotDegree = format("%.0f", temp - 0.005);
            //            if (hotDegree.contains(".00")) {
            //                hotDegree = hotDegree.replace(".00", "");
            //            } else if (hotDegree.contains(".") && hotDeStrgree.charAt(hotDegree.length() - 1) == '0') {
            //                hotDegree = hotDegree.replace(String.valueOf(hotDegree.charAt(hotDegree.length() - 1)), "");
            //            }
            hotDegree = format(Locale.CHINA, "%s万", hotDegree);
        }
        return hotDegree;
    }

    public static String getSmallImgUrl(String imgUrl) {
        if (TextUtils.isEmpty(imgUrl) || !imgUrl.contains("download.91playmate.cn")) {
            return imgUrl;
        } else {
            if (!imgUrl.contains("?x-oss-process=image"))
                imgUrl += "?x-oss-process=image/resize,m_lfit,h_320,w_320";
            return imgUrl;
        }
    }

    /**
     * 获取资源文件的id
     */
    public static int getRes(Context context, String name, String type) {
        return context.getResources().getIdentifier(name, type,
                context.getPackageName());
    }

    public static void copy(Context context, String text) {
        ClipData clip = ClipData.newPlainText("simple text", text);
        try {
            ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(clip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 数字转换成带有千位分隔符的西方格式
     */
    public static String numberToThousandsSeparator(String rawNum) {
        return numberToThousandsSeparator(rawNum, 2);
    }

    /**
     * 数字转换成带有千位分隔符的西方格式
     */
    public static String numberToThousandsSeparator(String rawNum, int decimalNum) {
        LogF.d("待转换数字", rawNum);
        if (!isNumber(rawNum)) {
            LogF.d("numberToThousandsSeparator", "不是数字");
            return rawNum;
        }
        if (!TextUtils.isEmpty(rawNum)) {

            String[] checkXiaoshu = rawNum.split("[.]");
            String shuzi = "";
            String decimal = "";
            if (checkXiaoshu.length == 2) {//有小数
                shuzi = checkXiaoshu[0];
                decimal = checkXiaoshu[1];
                if (decimalNum > 0)
                    if (decimal.length() > decimalNum) {
                        decimal = decimal.substring(0, decimalNum);
                    }
                decimal = clearZero(decimal);
                if (!TextUtils.isEmpty(decimal)) {
                    decimal = "." + decimal;
                }
            } else {
                shuzi = rawNum;
            }
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(shuzi);
            int len = shuzi.length();
            if (len > 3) {
                int num = 0;
                for (int i = len - 1; i >= 0; i--) {
                    num++;
                    if (num % 3 == 0 && i != 0) {//每3位+逗号 左边最后一位数字不作处理
                        spannableStringBuilder.insert(i, ",");
                    }
                }
                LogF.d("数字转换", spannableStringBuilder.toString() + "   decimal=" + decimal);
                return spannableStringBuilder.toString() + decimal;
            }
        }
        LogF.d("数字转换失败", rawNum);
        return rawNum;
    }

    /**
     * 去除末尾的0
     *
     * @param num 原始数字或者需要去零的数字
     */
    public static String clearZero(double num) {
        LogF.d("去零", "开始=" + num);
        return clearZero(num + "");
    }

    /**
     * 去除末尾的0
     *
     * @param rawNum 原始数字或者需要去零的字符
     */
    private static String clearZero(String rawNum) {
        LogF.d("去零", "开始=" + rawNum);
        if (TextUtils.isEmpty(rawNum)) {
            return "";
        }
        int decimalLen = rawNum.length();
        LogF.d("去零", "检查零的位置=" + rawNum.substring(decimalLen - 1, decimalLen));
        if (rawNum.lastIndexOf(".") != -1 && rawNum.substring(decimalLen - 1, decimalLen).equals("0")) {//末尾是0去除
            rawNum = rawNum.substring(0, decimalLen - 1);
            LogF.d("去零", "截取后=" + rawNum);
            rawNum = clearZero(rawNum);
        } else if (rawNum.substring(decimalLen - 1, decimalLen).equals(".")) {
            rawNum = rawNum.substring(0, decimalLen - 1);
            LogF.d("去.", "截取后=" + rawNum);
            rawNum = clearZero(rawNum);
        }
        LogF.d("去零", "最终=" + rawNum);
        return rawNum;
    }

    /**
     * 数字转换成带有千位分隔符的西方格式
     */
    public static String numberToThousandsSeparator(double rawNum) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
        String s = decimalFormat.format(rawNum);
        LogF.d("千位分隔符", "转换" + s);
        return s;
    }

    /**
     * 获取金额
     */
    public static String getCash(String rawNum, float Digits) {
        String cash;
        if (!isNumber(rawNum)) {
            LogF.d("numberToThousandsSeparator", "不是数字");
            ToastUtil.getInstances().showShort("输入无法识别,请重新输入");
            return rawNum;
        }
        if (!"0".equals(rawNum)) {
            double num = Double.valueOf(rawNum) / Digits;
            cash = getNumberNoZero(num);
        } else {
            cash = "0.0";
        }
        return cash;
    }

    /**
     * 将小数的".00"去掉使其符合整数  --->1.00->1
     */
    public static String getNumberNoZero(double rawNum) {
        LogF.d("保留小数点后2位去0", rawNum + "");
        DecimalFormat nf = new DecimalFormat("#.##");
        return nf.format(rawNum);
    }


    /**
     * 判断数字合法性
     */
    public static boolean isNumber(String rawNum) {
        boolean isNum = false;
        Pattern pattern = Pattern.compile("[0-9]");
        if (pattern.matcher(rawNum).matches()) {
            //数字
            isNum = true;
            LogF.d("isNumber数字检查", "数字" + rawNum);
        } else {
            //非数字
            LogF.d("isNumber数字检查", "非数字" + rawNum);
        }
        //带小数的
        pattern = Pattern.compile("^[0-9]+([.][0-9]+)?$");
        if (pattern.matcher(rawNum).matches()) {
            //数字
            LogF.d("isNumber数字检查", "小数" + rawNum);
            isNum = true;
        } else {
            //非数字
            LogF.d("isNumber数字检查", "非数字" + rawNum);
        }
        return isNum;
    }

    public static String getFormatNumber(double realNumber) {
        String number = String.format("%.2f", realNumber >= 10000 ? realNumber / 10000 : realNumber);
        while (number.endsWith("0") && number.contains(".")) {
            number = number.substring(0, number.length() - 1);
            if (number.endsWith(".")) {
                number = number.substring(0, number.length() - 1);
            }
        }
        return number;
    }

    /**
     * 车队时间格式化
     *
     * @param duration
     * @return
     */
    public static String formatCarDuration(String duration) {
        if (TextUtils.isEmpty(duration)) return "0";
        try {
            int min = Integer.valueOf(duration);
            int min2 = min / 30;
            if (min2 % 2 == 0) {
                return min2 / 2 + "";
            } else {
                return min2 / 2.0 + "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }


    }

    /**
     * 获取字符串的长度，如果有中文，则每个中文字符计为2位
     *
     * @param validateStr
     * @return
     */
    public static int getChineseLength(String validateStr) {
        int valueLength = 0;
        String chinese = "[\u0391-\uFFE5]";
        /* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
        for (int i = 0; i < validateStr.length(); i++) {
            /* 获取一个字符 */
            String temp = validateStr.substring(i, i + 1);
            /* 判断是否为中文字符 */
            if (temp.matches(chinese)) {
                /* 中文字符长度为2 */
                valueLength += 2;
            } else {
                /* 其他字符长度为1 */
                valueLength += 1;
            }
        }
        return valueLength;
    }


    /**
     * 字符串局部变色
     *
     * @param otherString  你要变色的字符，可为null
     * @param changeString 你要变色的字符
     * @param allString    整个字符串
     * @param colorString  要变颜色的色值
     *                     例: stringChangeColor(金额18元,"18元", null,#586E98);
     */
    public static SpannableStringBuilder stringChangeColor(String allString, String changeString, String otherString, String colorString) {
        int start = allString.indexOf(changeString);
        int end = start + changeString.length();

        SpannableStringBuilder style = new SpannableStringBuilder(allString);
        if (!"".equals(otherString) && otherString != null) {
            int bStart = allString.indexOf(otherString);
            int bEnd = bStart + otherString.length();
            style.setSpan(new ForegroundColorSpan(Color.parseColor(colorString)), bStart, bEnd,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        style.setSpan(new ForegroundColorSpan(Color.parseColor(colorString)), start, end,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

        return style;
    }

    //转换数组成List
    public static <E> List<E> transferArrayToList(E[] array) {
        List<E> transferedList = new ArrayList<>();
        Collections.addAll(transferedList, array);
        return transferedList;
    }

    public static byte[] short2byte(short s) {
        byte[] b = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = 16 - (i + 1) * 8; //因为byte占4个字节，所以要计算偏移量
            b[i] = (byte) ((s >> offset) & 0xff); //把16位分为2个8位进行分别存储
        }
        return b;
    }

    public static short byte2short(byte[] b) {
        short l = 0;
        for (int i = 0; i < 2; i++) {
            l <<= 8; //<<=和我们的 +=是一样的，意思就是 l = l << 8
            l |= (b[i] & 0xff); //和上面也是一样的  l = l | (b[i]&0xff)
        }
        return l;
    }

    public static byte[] int2byte(int s) {
        byte[] b = new byte[2];
        for (int i = 0; i < 4; i++) {
            int offset = 16 - (i + 1) * 8; //因为byte占4个字节，所以要计算偏移量
            b[i] = (byte) ((s >> offset) & 0xff); //把32位分为4个8位进行分别存储
        }
        return b;
    }

    public static int byte2int(byte[] b) {
        int l = 0;
        for (int i = 0; i < 4; i++) {
            l <<= 8; //<<=和我们的 +=是一样的，意思就是 l = l << 8
            l |= (b[i] & 0xff); //和上面也是一样的  l = l | (b[i]&0xff)
        }
        return l;
    }

}
