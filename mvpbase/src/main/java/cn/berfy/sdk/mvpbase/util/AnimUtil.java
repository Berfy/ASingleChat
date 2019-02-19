package cn.berfy.sdk.mvpbase.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.config.Gloabal;

/**
 * 动画效果
 */
public class AnimUtil {

    private static final String TAG = "AnimUtil";
    private static long mClickTime = 0;//点击跳转Ativity时间记录

    /**
     * 以平移方式跳转到下个Activity（该方法不会finish掉当前activity）
     *
     * @param current 当前的Activity
     * @param next    下一个Activity
     */
    public static void jump2NextPage(CommonActivity current, Class<?> next) {
        jump2NextPage(current, new Intent(current, next));
    }

    /**
     * 以平移方式跳转到下个Activity（该方法不会finish掉当前activity）
     *
     * @param current 当前的Activity
     */
    public static void jump2NextPage(Context current, Intent intent) {
        if (checkJump()) {
            current.startActivity(intent);
            try {
                ((Activity) current).overridePendingTransition(R.anim.translate_to_left_in, R.anim.translate_to_left_out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 以平移方式跳转到下个Activity（该方法不会finish掉当前activity）
     *
     * @param current 当前的Activity
     */
    public static void jump2NextPage(CommonActivity current, Intent intent) {
        if (checkJump()) {
            current.startActivity(intent);
            current.overridePendingTransition(R.anim.translate_to_left_in, R.anim.translate_to_left_out);
        }
    }

    /**
     * 以平移方式跳转到下个Activity（该方法不会finish掉当前activity）
     *
     * @param fragment 当前的Fragment
     */
    public static void jump2NextPageForResult(Fragment fragment, Intent intent, int requestCode) {
        if (checkJump()) {
            try {
                fragment.startActivityForResult(intent, requestCode);
                fragment.getActivity().overridePendingTransition(R.anim.translate_to_left_in, R.anim.translate_to_left_out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 以平移方式跳转到下个Activity（该方法不会finish掉当前activity）
     *
     * @param current 当前的Activity
     */
    public static void jump2NextPageForResult(Context current, Intent intent, int requestCode) {
        if (checkJump()) {
            try {
                ((Activity) current).startActivityForResult(intent, requestCode);
                ((Activity) current).overridePendingTransition(R.anim.translate_to_left_in, R.anim.translate_to_left_out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 以平移方式返回到上一个Activity
     *
     * @param current 当前的Activity
     */
    public static void jump2PrePage(Activity current) {
        current.finish();
        current.overridePendingTransition(R.anim.translate_to_right_in, R.anim.translate_to_right_out);
    }

    /**
     * 以平移方式返回到上一个Activity
     *
     * @param current 当前的Activity
     */
    public static void finishAnim(Activity current) {
        current.overridePendingTransition(R.anim.translate_to_right_in, R.anim.translate_to_right_out);
    }

    /**
     * 没有动画用渐变方式，避免黑屏
     *
     * @param current 当前的Activity
     */
    public static void noAnim(Activity current) {
        current.overridePendingTransition(R.anim.translate_to_hold, R.anim.translate_to_hold);
    }

    /**
     * 没有动画用渐变方式，避免黑屏
     *
     * @param current 当前的Activity
     */
    public static void noAnim1(Activity current) {
        current.overridePendingTransition(0, 0);
    }

    /*间隔跳转避免频繁操作*/
    public static boolean checkJump() {
        if (System.currentTimeMillis() - mClickTime >= Gloabal.ACTIVITY_INTENT_MIN_TIME) {
            mClickTime = System.currentTimeMillis();
            LogF.d("跳转监测", "可以跳转");
            return true;
        }
        LogF.d("跳转监测", "不可以跳转");
        return false;
    }
}
