package cn.berfy.sdk.mvpbase.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.config.Constant;
import cn.berfy.sdk.mvpbase.util.CommonUtil;
import cn.berfy.sdk.mvpbase.util.DeviceUtils;
import cn.berfy.sdk.mvpbase.util.LogF;

/**
 * Created by Berfy on 2017/8/15.
 * 状态栏间隔布局
 */
public class StatusMarginBar extends LinearLayout {

    private Context mContext;
    private final String TAG = "StatusMarginBar";
    public boolean mIsEnable = true;
    private int mColorBg;

    public StatusMarginBar(Context context) {
        super(context);
        mContext = context;
    }

    public StatusMarginBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.titleStyle);
            if (ta != null) {
                    boolean isLight = ta.getBoolean(R.styleable.titleStyle_lightTheme, false);
                    int color;
                    if (isLight) {
                        //                    setBgColor(R.color.color_titlebar_light_theme);
                        color = R.color.color_titlebar_light_theme;
                        LogF.d(TAG, "状态栏主题 高亮 " + CommonUtil.getColor(mContext, color));
                    } else {
                        //                    setBgColor(R.color.color_titlebar_normal_theme);
                        color = R.color.color_white;
                        LogF.d(TAG, "状态栏主题 正常 " + CommonUtil.getColor(mContext, color));
                    }
                mColorBg = ta.getColor(R.styleable.titleStyle_bar_back_color, CommonUtil.getColor(mContext, color));
                LogF.d(TAG, "状态栏主题 color " + mColorBg);
//                init();
            }
        }
    }

    public StatusMarginBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.titleStyle);
            if (ta != null) {
                boolean isLight = ta.getBoolean(R.styleable.titleStyle_lightTheme, false);
                int color;
                if (isLight) {
                    //                    setBgColor(R.color.color_titlebar_light_theme);
                    color = R.color.color_titlebar_light_theme;
                } else {
                    //                    setBgColor(R.color.color_titlebar_normal_theme);
                    color = R.color.color_white;
                }
                mColorBg = ta.getColor(R.styleable.titleStyle_bar_back_color, CommonUtil.getColor(mContext, color));
//                init();
            }
        }
    }

    private void initView() {
        mColorBg = CommonUtil.getColor(mContext, R.color.color_white);
    }

    public StatusMarginBar showStatusMarginBar(boolean isEnable) {
        mIsEnable = isEnable;
        return this;
    }

    public StatusMarginBar setBgColor(int bgColor) {
        mColorBg = CommonUtil.getColor(mContext, bgColor);
        return this;
    }

    public void commit() {
        init();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        init();
    }

    private void init() {
        try {
            if (Constant.ENABLE_TRANSLUCENT && mIsEnable) {
                LogF.d(TAG, "沉浸式状态栏 " + mIsEnable);
                LayoutParams layoutParams = (LayoutParams) getLayoutParams();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    //                    if (!AppUtils.checkDeviceHasNavigationBar(mContext)) {
                    setBackgroundColor(mColorBg);
                    layoutParams.height = DeviceUtils.getStatusBarHeight(mContext);
                    setLayoutParams(layoutParams);
                    //                    }
                }
                //                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //                        setBackgroundResource(R.color.color_bar_black_translate);
                //                    }
            } else {
                LogF.d(TAG, "非沉浸式状态栏 " + mIsEnable);
                LayoutParams layoutParams = (LayoutParams) getLayoutParams();
                layoutParams.height = 0;
                setLayoutParams(layoutParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
