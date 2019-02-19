package cn.berfy.sdk.mvpbase.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.util.CommonUtil;
import cn.berfy.sdk.mvpbase.util.LogF;

/**
 * Created by Berfy on 2017/10/13.
 * 通用标题栏
 */
public class TitleBarView extends LinearLayout {

    private final String TAG = "TitleBarView";
    private Context mContext;
    private View mRootView;
    private RelativeLayout mRlBg;
    private View mVLine;
    private ImageView mIvBack;
    private TextView mTvLeft;
    private TextView mTvTitle;
    private LinearLayout mLayoutRb;
    private RadioGroup mTitleRb;
    private TextView mTvRbTag;
    private LinearLayout mLlRight;
    private LinearLayout mLlLeft;
    private TextView mTvRight;
    private ImageView mIvRight;
    private StatusMarginBar mStatusMarginBar;

    public TitleBarView(Context context) {
        super(context);
        initView(context, null);
    }

    public TitleBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public TitleBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mContext = context;
        mRootView = View.inflate(mContext, R.layout.base_title_layout, null);
//        if (attrs != null) {
//            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.titleStyle);
//            if (ta != null) {
//                int color = ta.getColor(R.styleable.titleStyle_bar_back_color, -1);
//                if (color != -1) {
//                    setBarBackColor(color);
//                }
//            }
//        }
        setOrientation(VERTICAL);
        setFloatContent(mRootView);

        mStatusMarginBar = findViewById(R.id.statusBar);
        mRlBg = findViewById(R.id.layout_bg);
        mVLine = findViewById(R.id.v_line);
        mIvBack = findViewById(R.id.iv_back);
        mTvLeft = findViewById(R.id.tv_left);
        mTvTitle = findViewById(R.id.tv_title);
        mLayoutRb = findViewById(R.id.layout_rb);
        mTitleRb = findViewById(R.id.title_rb);
        mTvRbTag = findViewById(R.id.tv_rb_tag);
        mLlRight = findViewById(R.id.layout_right);
        mLlLeft = findViewById(R.id.layout_left);
        mTvRight = findViewById(R.id.tv_right);
        mIvRight = findViewById(R.id.iv_right);
        setOnBackListener(v -> {
            LogF.d(TAG, "关闭键盘");
            ((InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(((Activity) mContext).getWindow().getDecorView().getWindowToken(), 0);
            ((Activity) mContext).finish();
        });
        showStatusBarMargin(true);
        setBackground(getResources().getDrawable(R.drawable.title_bg));
    }

    public void showStatusBarMargin(boolean isShow) {
        mStatusMarginBar.showStatusMarginBar(isShow);
    }

    private void setFloatContent(View floatContent) {
        if (floatContent != null) {
            removeAllViews();
            ViewGroup parent = (ViewGroup) floatContent.getParent();
            if (parent != null) parent.removeView(floatContent);
            addView(floatContent, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            setVisibility(View.VISIBLE);
            floatContent.requestLayout();  //只加了这一行代码。7.0上必须要调用这段代码，否则view不会被测量、排版、绘制。
        }
    }

    public void setBackgroundColor(int colorResId) {
        mRlBg.setBackgroundColor(colorResId);
        mVLine.setVisibility(GONE);
    }

    @Override
    public void setBackground(Drawable background) {
//        super.setBackground(background);
        mRlBg.setBackground(background);
        mVLine.setVisibility(GONE);
    }

    public void setTitle(int titleResId) {
        setTitle(mContext.getString(titleResId));
    }

    public void setTitleColor(int titleColorId) {
        mTvTitle.setTextColor(titleColorId);
    }

    public void setTitle(String title) {
        setTextView(mTvTitle, title);
    }

    public void showRadioButton() {
        mTvTitle.setVisibility(View.GONE);
        mLayoutRb.setVisibility(View.VISIBLE);
    }

    public RadioGroup getRadioButton() {
        showRadioButton();
        return mTitleRb;
    }

    public TextView getRadioButtonTag() {
        showRadioButton();
        return mTvRbTag;
    }

    public void setOnBackListener(OnClickListener onClickListener) {
        mIvBack.setVisibility(View.VISIBLE);
        if (null != onClickListener)
            mLlLeft.setOnClickListener(onClickListener);
    }

    public void setLeftIcon(boolean isShow) {
        if (isShow) {
            mLlLeft.setVisibility(View.VISIBLE);
            mIvBack.setVisibility(View.VISIBLE);
            mTvLeft.setVisibility(GONE);
        } else {
            mLlLeft.setVisibility(View.INVISIBLE);
        }
    }

    public void setLeftIcon(boolean isShow, OnClickListener onClickListener) {
        if (isShow) {
            mLlLeft.setVisibility(View.VISIBLE);
            mIvBack.setVisibility(View.VISIBLE);
            mTvLeft.setVisibility(GONE);
        } else {
            mLlLeft.setVisibility(View.INVISIBLE);
        }
        if (null != onClickListener)
            mLlLeft.setOnClickListener(onClickListener);
    }

    public void setLeftIcon(boolean isShow, int iconResId, OnClickListener onClickListener) {
        if (isShow) {
            mLlLeft.setVisibility(View.VISIBLE);
            mIvBack.setVisibility(View.VISIBLE);
            mTvLeft.setVisibility(GONE);
        } else {
            mLlLeft.setVisibility(View.INVISIBLE);
        }
        mIvBack.setImageResource(iconResId);
        if (null != onClickListener)
            mLlLeft.setOnClickListener(onClickListener);
    }

    public void setLeftIcon(boolean isShow, int iconResId, ImageView.ScaleType scaleType, OnClickListener onClickListener) {
        if (isShow) {
            mLlLeft.setVisibility(View.VISIBLE);
            mIvBack.setVisibility(View.VISIBLE);
            mTvLeft.setVisibility(GONE);
        } else {
            mLlLeft.setVisibility(View.INVISIBLE);
        }
        mIvBack.setScaleType(scaleType);
        mIvBack.setImageResource(iconResId);
        if (null != onClickListener)
            mLlLeft.setOnClickListener(onClickListener);
    }

    public void setLeftTitle(boolean isShow) {
        if (isShow) {
            mLlLeft.setVisibility(View.VISIBLE);
            mIvBack.setVisibility(View.GONE);
            mTvLeft.setVisibility(View.VISIBLE);
        } else {
            mLlLeft.setVisibility(View.INVISIBLE);
        }
    }

    public void setLeftTitle(boolean isShow, OnClickListener onClickListener) {
        if (isShow) {
            mIvBack.setVisibility(View.VISIBLE);
        } else {
            mIvBack.setVisibility(View.INVISIBLE);
        }
        if (null != onClickListener)
            mLlLeft.setOnClickListener(onClickListener);
    }

    public void setLeftTitle(boolean isShow, String title) {
        if (isShow) {
            mLlLeft.setVisibility(View.VISIBLE);
            mIvBack.setVisibility(View.GONE);
            mTvLeft.setVisibility(View.VISIBLE);
        } else {
            mLlLeft.setVisibility(View.INVISIBLE);
        }
        setTextView(mTvLeft, title);
    }

    public void setLeftTitle(boolean isShow, String title, int colorResId) {
        if (isShow) {
            mLlLeft.setVisibility(View.VISIBLE);
            mIvBack.setVisibility(View.GONE);
            mTvLeft.setVisibility(View.VISIBLE);
        } else {
            mLlLeft.setVisibility(View.INVISIBLE);
        }
        setTextView(mTvLeft, title);
        mTvLeft.setTextColor(colorResId);
    }

    public void setLeftTitle(boolean isShow, String title, OnClickListener onClickListener) {
        if (isShow) {
            mLlLeft.setVisibility(View.VISIBLE);
            mIvBack.setVisibility(View.GONE);
            mTvLeft.setVisibility(View.VISIBLE);
        } else {
            mLlLeft.setVisibility(View.INVISIBLE);
        }
        setTextView(mTvLeft, title);
        if (null != onClickListener)
            mLlLeft.setOnClickListener(onClickListener);
    }

    public void setLeftTitle(boolean isShow, int titleResId, OnClickListener onClickListener) {
        if (isShow) {
            mLlLeft.setVisibility(View.VISIBLE);
            mIvBack.setVisibility(View.GONE);
            mTvLeft.setVisibility(View.VISIBLE);
        } else {
            mLlLeft.setVisibility(View.INVISIBLE);
        }
        setTextView(mTvLeft, titleResId);
        if (null != onClickListener)
            mLlLeft.setOnClickListener(onClickListener);
    }

    public void setLeftTitle(boolean isShow, int titleRes, int colorResId, OnClickListener onClickListener) {
        if (isShow) {
            mLlLeft.setVisibility(View.VISIBLE);
            mIvBack.setVisibility(View.GONE);
            mTvLeft.setVisibility(View.VISIBLE);
        } else {
            mLlLeft.setVisibility(View.INVISIBLE);
        }
        setTextView(mTvLeft, titleRes);
        mTvLeft.setTextColor(colorResId);
        if (null != onClickListener)
            mLlLeft.setOnClickListener(onClickListener);
    }

    public void setLeftTitle(boolean isShow, String title, int colorResId, OnClickListener onClickListener) {
        if (isShow) {
            mLlLeft.setVisibility(View.VISIBLE);
            mIvBack.setVisibility(View.GONE);
            mTvLeft.setVisibility(View.VISIBLE);
        } else {
            mLlLeft.setVisibility(View.INVISIBLE);
        }
        setTextView(mTvLeft, title);
        mTvLeft.setTextColor(colorResId);
        if (null != onClickListener)
            mLlLeft.setOnClickListener(onClickListener);
    }

    public void setLeftClick(OnClickListener onClickListener) {
        if (null != onClickListener)
            mLlLeft.setOnClickListener(onClickListener);
    }

    public View getLeftView() {
        return mLlLeft;
    }

    public View getRightView() {
        return mLlRight;
    }

    public void setRightTitle(boolean isShow) {
        if (isShow) {
            mLlRight.setVisibility(View.VISIBLE);
            mIvRight.setVisibility(GONE);
        } else {
            mLlRight.setVisibility(View.INVISIBLE);
        }
    }

    public void setRightTitle(boolean isShow, String title) {
        if (isShow) {
            mLlRight.setVisibility(View.VISIBLE);
            mIvRight.setVisibility(GONE);
        } else {
            mLlRight.setVisibility(View.INVISIBLE);
        }
        setTextView(mTvRight, title);
    }

    public void setRightTitle(boolean isShow, int titleRes) {
        if (isShow) {
            mLlRight.setVisibility(View.VISIBLE);
            mIvRight.setVisibility(GONE);
        } else {
            mLlRight.setVisibility(View.INVISIBLE);
        }
        setTextView(mTvRight, titleRes);
    }

    public void setRightTitle(boolean isShow, String title, int colorResId) {
        if (isShow) {
            mLlRight.setVisibility(View.VISIBLE);
            mIvRight.setVisibility(GONE);
        } else {
            mLlRight.setVisibility(View.INVISIBLE);
        }
        setTextView(mTvRight, title);
        mTvRight.setTextColor(colorResId);
    }

    public void setRightTitle(boolean isShow, String title, OnClickListener onClickListener) {
        if (isShow) {
            mLlRight.setVisibility(View.VISIBLE);
            mIvRight.setVisibility(GONE);
        } else {
            mLlRight.setVisibility(View.INVISIBLE);
        }
        setTextView(mTvRight, title);
        if (null != onClickListener)
            mLlRight.setOnClickListener(onClickListener);
    }

    public void setRightTitle(boolean isShow, int titleResId, OnClickListener onClickListener) {
        if (isShow) {
            mLlRight.setVisibility(View.VISIBLE);
            mIvRight.setVisibility(GONE);
        } else {
            mLlRight.setVisibility(View.INVISIBLE);
        }
        setTextView(mTvRight, titleResId);
        if (null != onClickListener)
            mLlRight.setOnClickListener(onClickListener);
    }

    public void setRightTitle(boolean isShow, String title, int colorResId, OnClickListener onClickListener) {
        if (isShow) {
            mLlRight.setVisibility(View.VISIBLE);
            mIvRight.setVisibility(GONE);
        } else {
            mLlRight.setVisibility(View.INVISIBLE);
        }
        setTextView(mTvRight, title);
        mTvRight.setTextColor(CommonUtil.getColor(mContext, colorResId));
        if (null != onClickListener)
            mLlRight.setOnClickListener(onClickListener);
    }

    public void setRightClick(OnClickListener onClickListener) {
        if (null != onClickListener)
            mLlRight.setOnClickListener(onClickListener);
    }

    public void setRightIcon(boolean isShow) {
        if (isShow) {
            mLlRight.setVisibility(View.VISIBLE);
        } else {
            mLlRight.setVisibility(View.INVISIBLE);
        }
    }

    public void setRightIcon(boolean isShow, int resId, OnClickListener onClickListener) {
        if (isShow) {
            mLlRight.setVisibility(View.VISIBLE);
            mIvRight.setVisibility(View.VISIBLE);
            mTvRight.setVisibility(View.GONE);
        } else {
            mLlRight.setVisibility(View.INVISIBLE);
        }
        mIvRight.setBackgroundResource(resId);
        if (null != onClickListener)
            mLlRight.setOnClickListener(onClickListener);
    }

    public void setRightIcon(boolean isShow, Drawable drawable, OnClickListener onClickListener) {
        if (isShow) {
            mLlRight.setVisibility(View.VISIBLE);
            mIvRight.setVisibility(View.VISIBLE);
            mTvRight.setVisibility(View.GONE);
        } else {
            mLlRight.setVisibility(View.INVISIBLE);
        }
        if (null != drawable)
            mIvRight.setBackground(drawable);
        if (null != onClickListener)
            mLlRight.setOnClickListener(onClickListener);
    }

    public void setRightIcon(boolean isShow, Bitmap bitmap, OnClickListener onClickListener) {
        if (isShow) {
            mLlRight.setVisibility(View.VISIBLE);
            mIvRight.setVisibility(View.VISIBLE);
            mTvRight.setVisibility(View.GONE);
        } else {
            mLlRight.setVisibility(View.INVISIBLE);
        }
        if (null != bitmap)
            mIvRight.setImageBitmap(bitmap);
        if (null != onClickListener)
            mLlRight.setOnClickListener(onClickListener);
    }

    private void setTextView(TextView tv, int resId) {
        tv.setText(mContext.getString(resId));
    }

    public void setTextView(TextView tv, String text) {
        if (!TextUtils.isEmpty(text))
            tv.setText(text);
    }

    //次页面标题栏
    public void showNormalTheme(){
        mStatusMarginBar.setBgColor(R.color.color_titlebar_normal_theme);
        mIvBack.setImageResource(R.mipmap.ic_arrow_normal);
        setBackgroundColor(getResources().getColor(R.color.color_titlebar_normal_theme));
        setTitleColor(getResources().getColor(R.color.color_titlebar_normal_title));
        mTvLeft.setTextColor(getResources().getColor(R.color.color_titlebar_normal_small_title));
        mTvRight.setTextColor(getResources().getColor(R.color.color_titlebar_normal_small_title));
    }

    /**
     * 设置阴影线的显示
     *
     * @param isVisible
     */
    public void setLineViewVisible(boolean isVisible) {
        mVLine.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }
}
