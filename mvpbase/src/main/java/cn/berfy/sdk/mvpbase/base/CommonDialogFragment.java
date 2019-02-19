package cn.berfy.sdk.mvpbase.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.iview.IBaseView;
import cn.berfy.sdk.mvpbase.listener.CommonFragmentListener;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.DisplayUtil;
import cn.berfy.sdk.mvpbase.util.LogF;

/**
 * DailogFragment的基类
 *
 * @author Berfy
 */
public abstract class CommonDialogFragment<V extends IBaseView, T extends BasePresenter<V>> extends DialogFragment {

    private final String TAG = "CommonDialogFragment";
    protected T mPresenter;
    /**
     * 页面名称
     */
    private String C_PAGE_NAME;
    //定向传递需要双方保持id一致，即接收和发送方一致，不判断id一致则群发
    protected long mEventBusId;//本次id、用于定向传递给下一个Activitry发生关系
    protected long mPreEventBusId;//被传递过来的Id，用于定向给上一个Activity发生关系
    private final String LAST_EVENTBUS_ID = "last_eventbus_id";
    protected View mRootView;
    private Unbinder mUnbinder;
    protected Context mContext;
    private CommonFragmentListener mCommonFragmentListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (null != mCommonFragmentListener) {
            LogF.d(TAG, "onCreate  " + mContext);
            mCommonFragmentListener.onCreate(savedInstanceState);
        }
        doSomething();
        initData(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void doSomething() {
        mEventBusId = System.currentTimeMillis();
        if (null != getArguments())
            mPreEventBusId = getArguments().getLong(LAST_EVENTBUS_ID, 0);
    }

    /**
     * 生命周期监听
     *
     * @param commonListener
     */
    public void setCommonListener(CommonFragmentListener commonListener) {
        mCommonFragmentListener = commonListener;
    }

    /**
     * 设置页面统计名称
     *
     * @param name
     */
    protected void setStatisticsName(String name) {
        C_PAGE_NAME = name;
    }

    /**
     * 设置页面统计名称
     *
     * @param resId
     */
    protected void setStatisticsName(@StringRes int resId) {
        setStatisticsName(getString(resId));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mCommonFragmentListener) {
            LogF.d(TAG, "onResume  " + mContext);
            mCommonFragmentListener.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mCommonFragmentListener) {
            LogF.d(TAG, "onPause  " + mContext);
            mCommonFragmentListener.onPause();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(getContentViewId(), container, false);
            mUnbinder = ButterKnife.bind(this, mRootView);
        }
        if (null != mCommonFragmentListener) {
            LogF.d(TAG, "onCreateView  " + mContext);
            mCommonFragmentListener.onCreateView(inflater, container, savedInstanceState);
        }
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().setCancelable(true);
        getDialog().setCanceledOnTouchOutside(true);
        Window window = getDialog().getWindow();
        window.setGravity(Gravity.CENTER);
        window.getDecorView().setPadding(0, DisplayUtil.dip2px(mContext, 30), 0, DisplayUtil.dip2px(mContext, 30)); //消除边距
        WindowManager.LayoutParams lp = window.getAttributes();
        if (getWidth() > 0) {
            lp.width = getWidth();
        } else {
            lp.width = DisplayUtil.getDisplayWidth(mContext) / 4 * 3;
        }
        if (getHeight() > 0) {
            lp.height = getHeight();   //设置高度
        } else {
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        window.setAttributes(lp);
        return mRootView;
    }

    public void show(FragmentActivity activity) {
        LogF.d(TAG, "显示dialog");
        show(activity.getSupportFragmentManager(), "");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mPresenter = initPresenter();
        if (null != mPresenter)
            mPresenter.attach(mContext, (V) this);
        initView();
        if (null != mCommonFragmentListener) {
            LogF.d(TAG, "viewCreated  " + mContext);
            mCommonFragmentListener.onViewCreated(view, savedInstanceState);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    public <T extends View> T findViewById(int id) {
        return mRootView.findViewById(id);
    }

    protected View getRootView() {
        return mRootView;
    }

    @Override
    public void registerForContextMenu(View view) {
        super.registerForContextMenu(view);
    }

    @Override
    public void startActivity(Intent intent) {
        //透传当前Activity的eventBusId
        intent.putExtra(LAST_EVENTBUS_ID, mEventBusId);
        super.startActivity(intent);
        getActivity().overridePendingTransition(R.anim.translate_to_left_in, R.anim.translate_to_left_out);
    }

    // 获取布局文件用于加载布局
    public abstract int getContentViewId();

    // 加载数据
    public abstract void initData(Bundle savedInstanceState);

    // 加载控件
    public abstract void initView();

    // 用初始化presenter
    public abstract T initPresenter();

    // dailog宽度
    public abstract int getWidth();

    // dailog高度
    public abstract int getHeight();

    public T getPresenter() {
        return mPresenter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mUnbinder) {
            mUnbinder.unbind();
        }
        if (null != mCommonFragmentListener) {
            LogF.d(TAG, "onDestroyView  " + mContext);
            mCommonFragmentListener.onDestroyView();
        }
        if (null != mPresenter)
            mPresenter.detach();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (null != mCommonFragmentListener) {
            LogF.d(TAG, "onStop  " + mContext);
            mCommonFragmentListener.onStop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mCommonFragmentListener) {
            LogF.d(TAG, "onDestroy  " + mContext);
            mCommonFragmentListener.onDestroy();
        }
    }
}
