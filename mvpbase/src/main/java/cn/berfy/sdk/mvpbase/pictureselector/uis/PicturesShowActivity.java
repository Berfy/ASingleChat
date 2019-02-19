package cn.berfy.sdk.mvpbase.pictureselector.uis;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.base.BaseApplication;
import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.pictureselector.entities.ImageEntity;
import cn.berfy.sdk.mvpbase.pictureselector.views.ViewPagerFixed;
import cn.berfy.sdk.mvpbase.pictureselector.uis.fragments.ScaleImageFragment;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSConstanceUtil;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.pictureselector.uis.adapters.ReviewAdapter;
import cn.berfy.sdk.mvpbase.util.DisplayUtil;
import cn.berfy.sdk.mvpbase.util.LogF;

public class PicturesShowActivity extends CommonActivity implements ViewPager.OnPageChangeListener {

    private ViewPagerFixed mViewPager;
    private ReviewAdapter mReviewAdapter;
    public static String INTENT_EXTRA_IMG = "imgs";
    public static String INTENT_EXTRA_POSITION = "position";
    public static String INTENT_EXTRA_IS_DELETE = "isDelete";
    public static String INTENT_EXTRA_CLICKVIEW_X = "clickView_x";
    public static String INTENT_EXTRA_CLICKVIEW_Y = "clickView_y";
    public static String INTENT_EXTRA_CLICKVIEW_WIDTH = "clickView_width";
    public static String INTENT_EXTRA_CLICKVIEW_HEIGHT = "clickView_height";
    private int mPosition;
    private boolean mIsCanDelete;
    private ArrayList<ImageEntity> mItems;

    private int mClickView_X;//位置和宽高
    private int mClickView_Y;
    private int mClickView_WIDTH;
    private int mClickView_HEIGHT;

    @Override
    public int getContentViewId() {
        return R.layout.ps_pictures_show;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mItems = getIntent().getParcelableArrayListExtra(INTENT_EXTRA_IMG);
        if (null == mItems) {
            close();
        }
        mPosition = getIntent().getIntExtra(INTENT_EXTRA_POSITION, 0);
        mIsCanDelete = getIntent().getBooleanExtra(INTENT_EXTRA_IS_DELETE, false);
        EventBus.getDefault().register(this);
    }

    @Override
    public void initView() {
        showTitleBar();
        getTitleBar().setTitle(getTitleText());
        if (mIsCanDelete) {
            getTitleBar().setLeftTitle(true, R.string.cancel, v -> close());
            getTitleBar().setRightIcon(true, R.drawable.ps_ic_picture_delete, v -> {
                int position = mViewPager.getCurrentItem();
                mItems.remove(position);
                if (mItems.size() == 0) {//最后一张
                    close();
                } else {
                    if (position > 0) {
                        position--;
                    }
                    mPosition = position;
                    mReviewAdapter.setData(mItems);
                    mViewPager.setAdapter(mReviewAdapter);
                    mViewPager.setCurrentItem(position);
                    getTitleBar().setTitle(getTitleText());
                }
            });
        } else {
        }
        mViewPager = findViewById(R.id.vp_images);
        mViewPager.setOffscreenPageLimit(1);
        mReviewAdapter = new ReviewAdapter(getSupportFragmentManager());
        mReviewAdapter.setData(mItems);

        if (getIntent().hasExtra(INTENT_EXTRA_CLICKVIEW_X)) {
            mClickView_X = getIntent().getIntExtra(INTENT_EXTRA_CLICKVIEW_X, 0);
            mClickView_Y = getIntent().getIntExtra(INTENT_EXTRA_CLICKVIEW_Y, 0);
            mClickView_WIDTH = getIntent().getIntExtra(INTENT_EXTRA_CLICKVIEW_WIDTH, 0);
            mClickView_HEIGHT = getIntent().getIntExtra(INTENT_EXTRA_CLICKVIEW_HEIGHT, 0);
            showHoldAnim(300);
            showData();
            BaseApplication.getMainThreadHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showAnim(true,400);
                }
            }, 300);
        } else {
            showData();
        }
    }

    private void showData() {
        mViewPager.setAdapter(mReviewAdapter);
        mViewPager.setCurrentItem(mPosition);
        mViewPager.setOnPageChangeListener(this);
    }

    private void showHoldAnim(int duration) {
        float scale = mClickView_WIDTH * 1f / DisplayUtil.getDisplayWidth(mContext);
        int dx = mClickView_X + mClickView_WIDTH / 2 - DisplayUtil.getDisplayWidth(mContext) / 2;
        int dy = mClickView_Y + mClickView_HEIGHT / 2 - ((DisplayUtil.getDisplayHeight(mContext) - DisplayUtil.getStatusBarHeight(mContext)
                - DisplayUtil.dip2px(50)) / 2 + DisplayUtil.dip2px(50) + DisplayUtil.getStatusBarHeight(mContext));
        LogF.d("多图查看", "View位置 x=" + mClickView_X + ",y=" + mClickView_Y);
        LogF.d("多图查看", "View宽高 width=" + mClickView_WIDTH + ",height=" + mClickView_HEIGHT);
        AnimationSet animationSet = new AnimationSet(false);
        ObjectAnimator translationX = new ObjectAnimator().ofFloat(mViewPager, "translationX",
                dx,
                dx);
        ObjectAnimator translationY = new ObjectAnimator().ofFloat(mViewPager, "translationY",
                dy,
                dy);
        LogF.d("多图查看", "动画平移 dx=" + dx + ",dy=" + dy);
        ObjectAnimator scaleAnimX = ObjectAnimator.ofFloat(mViewPager, "scaleX",
                scale, scale);
        ObjectAnimator scaleAnimY = ObjectAnimator.ofFloat(mViewPager, "scaleY",
                scale, scale);
        LogF.d("多图查看", "动画缩放 scale=" + scale);
        AnimatorSet animatorSet = new AnimatorSet();  //组合动画
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animatorSet.playTogether(translationX, translationY, scaleAnimX, scaleAnimY); //设置动画
        animatorSet.setDuration(duration);  //设置动画时间
        animatorSet.start(); //启动
    }

    private void closeAnim() {
        showAnim(false, 400);
    }

    private void showAnim(boolean isShow, int duration) {
        float scale = mClickView_WIDTH * 1f / DisplayUtil.getDisplayWidth(mContext);
        int dx = mClickView_X + mClickView_WIDTH / 2 - DisplayUtil.getDisplayWidth(mContext) / 2;
        int dy = mClickView_Y + mClickView_HEIGHT / 2 - ((DisplayUtil.getDisplayHeight(mContext) - DisplayUtil.getStatusBarHeight(mContext)
                - DisplayUtil.dip2px(50)) / 2 + DisplayUtil.dip2px(50) + DisplayUtil.getStatusBarHeight(mContext));
        LogF.d("多图查看", "View位置 x=" + mClickView_X + ",y=" + mClickView_Y);
        LogF.d("多图查看", "View宽高 width=" + mClickView_WIDTH + ",height=" + mClickView_HEIGHT);
        AnimationSet animationSet = new AnimationSet(false);
        ObjectAnimator translationX = new ObjectAnimator().ofFloat(mViewPager, "translationX",
                isShow ? dx : 0,
                isShow ? 0 : dx);
        ObjectAnimator translationY = new ObjectAnimator().ofFloat(mViewPager, "translationY",
                isShow ? dy : 0,
                isShow ? 0 : dy);
        LogF.d("多图查看", "动画平移 dx=" + dx + ",dy=" + dy);
        ObjectAnimator scaleAnimX = ObjectAnimator.ofFloat(mViewPager, "scaleX",
                isShow ? scale : 1f, isShow ? 1f : scale);
        ObjectAnimator scaleAnimY = ObjectAnimator.ofFloat(mViewPager, "scaleY",
                isShow ? scale : 1f, isShow ? 1f : scale);
        LogF.d("多图查看", "动画缩放 scale=" + scale);
        AnimatorSet animatorSet = new AnimatorSet();  //组合动画
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animatorSet.playTogether(translationX, translationY, scaleAnimX, scaleAnimY); //设置动画
        animatorSet.setDuration(duration);  //设置动画时间
        animatorSet.start(); //启动
    }

    @Override
    public BasePresenter initPresenter() {
        return null;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mPosition = position;
        getTitleBar().setTitle(getTitleText());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private String getTitleText() {
        if (null != mItems) {
            return (mPosition + 1) + "/" + mItems.size();
        }
        return "";
    }

    /**
     * Page中的图片被点击
     */
    @SuppressWarnings("UnusedParameters")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ScaleImageFragment.OnImageClicked onImageClicked) {
        close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void close() {
        if (mIsCanDelete) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(PSConstanceUtil.PASS_SELECTED, mItems);
            setResult(Activity.RESULT_OK, intent);
        }
        closeAnim();
        BaseApplication.getMainThreadHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                overridePendingTransition(R.anim.translate_to_hold, R.anim.translate_to_hold_dismiss);
            }
        }, 300);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            close();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
