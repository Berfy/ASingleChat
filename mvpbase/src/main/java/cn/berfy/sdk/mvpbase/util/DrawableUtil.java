package cn.berfy.sdk.mvpbase.util;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

public class DrawableUtil {
    /**
     * 动态生成Drawable对象，和使用xml的shape标签同等
     *
     * @param radius
     * @param argb
     * @return
     */
    public static GradientDrawable generateDrawable(float radius, int argb) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);//设置矩形形状
        drawable.setCornerRadius(radius);//设置圆角角度
        drawable.setColor(argb);//设置填充色
        return drawable;
    }

    /**
     * 动态生成Selector
     *
     * @param pressed
     * @param normal
     * @return
     */
    public static StateListDrawable generateSelector(Drawable selected, Drawable pressed, Drawable normal) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, selected);
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressed);
        stateListDrawable.addState(new int[]{}, normal);//设置默认状态的图片
        return stateListDrawable;
    }

    /**
     * 销毁帧动画
     *
     * @param drawable
     */
    public static void destroyAnim(AnimationDrawable drawable) {
        if (drawable != null) {
            drawable.stop();
            for (int i = 0; i < drawable.getNumberOfFrames(); i++) {
                Drawable frame = drawable.getFrame(i);
                if (frame != null && frame instanceof BitmapDrawable) {
                    if (((BitmapDrawable) frame).getBitmap() != null)
                        ((BitmapDrawable) frame).getBitmap().recycle();
                    frame.setCallback(null);
                }

            }
            drawable.setCallback(null);
        }
    }
}
