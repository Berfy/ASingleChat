package cn.berfy.sdk.mvpbase.view;

import android.content.Context;

import android.content.res.TypedArray;

import android.graphics.*;

import android.support.annotation.ColorInt;
import android.util.AttributeSet;

import android.widget.TextView;

import cn.berfy.sdk.mvpbase.R;


/**
 * 自定义形状的TextView 圆形 椭圆形
 * <p>
 * Created by cjy on 16/11/30.
 */

public class CustomShapTextView extends TextView {

    private Context mContext;

    /**
     * 画笔
     */
    private Paint mPaint;

    /**
     * 画笔颜色 默认灰色
     */

    private int mColor = 0xFFDCDCDC;

    /**
     * 是否填充颜色
     */

    private boolean isFillColor = false;


    public CustomShapTextView(Context context) {

        super(context);

    }


    public CustomShapTextView(Context context, AttributeSet attrs) {

        super(context, attrs);

        initPaint(context, attrs);

    }


    public CustomShapTextView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);

        initPaint(context, attrs);

    }


    /**
     * 初始化画笔和自定义属性
     *
     * @param context
     * @param attrs
     */

    private void initPaint(Context context, AttributeSet attrs) {
        mContext = context;
        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.CustomShapTextView);
        mColor = typeArray.getColor(R.styleable.CustomShapTextView_bgColor, mColor);
        mPaint = new Paint();
        //抗锯齿
        mPaint.setAntiAlias(true);
        //画笔颜色
        mPaint.setColor(mColor);
    }

    public void setBackgroundColor(@ColorInt int color) {
        mColor = color;
        //画笔颜色
        mPaint.setColor(mColor);
        invalidate();
    }

    /**
     * 调用onDraw绘制边框
     *
     * @param canvas
     */

    @Override

    protected void onDraw(Canvas canvas) {
        if (isFillColor) {
            mPaint.setStyle(Paint.Style.FILL);

        } else {
            mPaint.setStyle(Paint.Style.STROKE);
        }
        //创建一个区域,限制圆弧范围

        RectF rectF = new RectF();

        //设置半径,比较长宽,取最大值

        int radius = getMeasuredWidth() > getMeasuredHeight() ? getMeasuredWidth() : getMeasuredHeight();

        //设置Padding 不一致,绘制出的是椭圆;一致的是圆形

        rectF.set(getPaddingLeft(), getPaddingTop(), radius - getPaddingRight(), radius - getPaddingBottom());

        //绘制圆弧

        canvas.drawArc(rectF, 0, 360, false, mPaint);


        //最后调用super方法,解决文本被所绘制的圆圈背景锁覆盖的问题

        super.onDraw(canvas);

    }


    /**
     * 设置是否填充颜色
     *
     * @param isFill
     */

    public void setFillColor(boolean isFill) {

        this.isFillColor = isFill;

        invalidate();

    }

}