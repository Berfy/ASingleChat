package cn.berfy.sdk.mvpbase.base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

import butterknife.ButterKnife;
import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.config.Constant;
import cn.berfy.sdk.mvpbase.iview.IBaseView;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.AppUtils;
import cn.berfy.sdk.mvpbase.util.DeviceUtils;
import cn.berfy.sdk.mvpbase.util.HostSettings;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.view.StatusMarginBar;
import cn.berfy.sdk.mvpbase.view.TitleBarView;
import cn.berfy.sdk.mvpbase.view.dialog.CommonDialog;

/**
 * Activity基类
 *
 * @author Berfy
 * @date 2017/12/30
 */
public abstract class CommonActivity<V extends IBaseView, T extends BasePresenter<V>> extends AppCompatActivity implements OnPermission {

    protected static final String TAG = "CommonActivity";

    protected T mPresenter;
    protected Activity mContext;
    private LinearLayout mLayoutBase;//界面根布局
    private LinearLayout mLayoutContent;//界面容器
    protected TitleBarView mTitleBarView;
    protected FragmentManager mFragmentManager;
    protected CommonDialog mCommonDialog;
    private CheckPermListener mListener; //权限回调接口

    //定向传递需要双方保持id一致，即接收和发送方一致，不判断id一致则群发
    protected long mEventBusId;//本次id、用于定向传递给下一个Activitry发生关系
    protected long mPreEventBusId;//被传递过来的Id，用于定向给上一个Activity发生关系
    private final String LAST_EVENTBUS_ID = "last_eventbus_id";

    private StatusMarginBar mStatusMarginBar;

    private int mSystemUiVisibility = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(null);
        LogF.d(TAG, getClass().getSimpleName() + "====onCreate()");
        //把Activity的引用放到集合中
        ActivityManager.getInstance().pushActivity(this);
        mFragmentManager = getSupportFragmentManager();
        mContext = this;
        mCommonDialog = new CommonDialog(mContext);
        doSomething();
        initData(savedInstanceState);
        setPresenter();
        setBaseContentView();
        navigationBar();
        registerReceiver();
        initView();
    }

    private void setBaseContentView() {
        setStatusTransparent();
        darkStatusBar(true);
        setContentView(R.layout.base_activity);
        mLayoutBase = findViewById(R.id.layout_base);
        mStatusMarginBar = findViewById(R.id.common_statusBar);
        mLayoutContent = findViewById(R.id.common_layout_content);
        mTitleBarView = findViewById(R.id.common_titlebarview);
        mTitleBarView.setVisibility(View.GONE);
        mLayoutContent.addView(View.inflate(mContext, getContentViewId(), null), mLayoutContent.getLayoutParams());
        showStatusMarginBar(false);
        showHost();
        ButterKnife.bind(this);
    }

    /**
     * 说明：Android 4.4+ 设置状态栏透明
     */
    public void setStatusTransparent() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 5.0+ 实现
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 4.4 实现
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    //沉浸式处理
    private void navigationBar() {
        DeviceUtils.setMeizuStatusBarDarkIcon(this, true);
        DeviceUtils.setMiuiStatusBarDarkMode(this, true);
        //包含虚拟按键的手机暂时不做沉浸式处理，背景色主色调
        boolean isHas = AppUtils.checkDeviceHasNavigationBar(mContext);
        if (isHas) {
            /*if (cancelWrapContentView()) {
                getWindow().getDecorView().setFitsSystemWindows(true);
            } else {
                findViewById(R.id.layout_base).setFitsSystemWindows(true);
            }
            //去掉透明
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().setNavigationBarColor(CommonUtil.getColor(com.axingxing.component.basiclib.R.color.common_colorPrimary));
            }
            lightStatusBar(true);*/
        }
    }

    private void setPresenter() {
        mPresenter = initPresenter();
        if (null != mPresenter)
            mPresenter.attach(mContext, (V) this);
    }

    protected void showHost() {
        TextView tv_host_status = findViewById(R.id.tv_host_status);
        if (Constant.DEBUG) {
            //动态显示服务器
            switch (HostSettings.getHostType()) {
                case HostSettings.HOST_TYPE_DEV:
                    tv_host_status.setVisibility(View.VISIBLE);
                    tv_host_status.setText("开发");
                    break;
                case HostSettings.HOST_TYPE_TEST:
                    tv_host_status.setVisibility(View.VISIBLE);
                    tv_host_status.setText("测试");
                    break;
                case HostSettings.HOST_TYPE_PREPARED:
                    tv_host_status.setVisibility(View.VISIBLE);
                    tv_host_status.setText("封测");
                    break;
                case HostSettings.HOST_TYPE_PRO:
                    tv_host_status.setVisibility(View.VISIBLE);
                    tv_host_status.setText("正式");
                    break;
            }
        } else {
            tv_host_status.setVisibility(View.GONE);
        }
    }

    private void registerReceiver() {
        //注册广播接收器(接收用户被抢登及冻结的广播)
        IntentFilter filter = new IntentFilter();
        //公共操作
        registerReceiver(mCommonReceiver, filter);
    }

    /**
     * 状态栏黑色设置
     */
    public void darkStatusBar(boolean isDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            if (decorView == null) return;
            if (-1 == mSystemUiVisibility) {//保存默认UI主题
                mSystemUiVisibility = decorView.getSystemUiVisibility();
            } else {
                decorView.setSystemUiVisibility(mSystemUiVisibility);
            }
            int vis = decorView.getSystemUiVisibility();
            if (isDark) {
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(vis);
            LogF.d(TAG, "黑色标题栏 " + isDark);
        }
    }

    public void setFitsSystemWindows(boolean isFit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (null != mLayoutBase) {
                mLayoutBase.setFitsSystemWindows(isFit);
            }
        }
    }

    //正常主题
    public void showNormalTheme() {
        if (null != mTitleBarView)
            mTitleBarView.showNormalTheme();
        darkStatusBar(true);
        if (null != mStatusMarginBar)
            mStatusMarginBar.setBgColor(R.color.color_f8df9d);
    }

    public void setBackgroundResource(int backgroundRes) {
        if (null != mLayoutBase) {
            mLayoutBase.setBackgroundResource(backgroundRes);
        }
    }

    public T getPresenter() {
        return mPresenter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogF.d(TAG, getClass().getSimpleName() + "====onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogF.d(TAG, getClass().getSimpleName() + "====onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogF.d(TAG, getClass().getSimpleName() + "====onStop()");
    }

    public void setHideVirtualKey(Window window) {
        //保持布局状态
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                //布局位于状态栏下方
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                //全屏
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                //隐藏导航栏
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= 19) {
            uiOptions |= 0x00001000;
        } else {
            uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        window.getDecorView().setSystemUiVisibility(uiOptions);
    }

    /**
     * 沉浸式布局占位
     *
     * @param isShow 是否显示占位状态栏
     */
    protected void showStatusMarginBar(boolean isShow) {
        if (null != mStatusMarginBar) {
            LogF.d(TAG, "设置showStatusMarginBar=" + isShow);
            mStatusMarginBar.showStatusMarginBar(isShow);
        }
    }

    protected void showStatusMarginBar(boolean isShow, int bgColor) {
        if (null != mStatusMarginBar) {
            mStatusMarginBar.showStatusMarginBar(isShow)
                    .setBgColor(bgColor)
                    .commit();
        }
    }

    protected void showTitleBar() {
        mTitleBarView.setVisibility(View.VISIBLE);
    }

    protected TitleBarView getTitleBar() {
        return mTitleBarView;
    }

    private void doSomething() {
        mEventBusId = System.currentTimeMillis();
        mPreEventBusId = getIntent().getLongExtra(LAST_EVENTBUS_ID, 0);
    }

    // 获取布局文件用于加载布局
    public abstract int getContentViewId();

    // 在布局文件加载前做数据操作
    public abstract void initData(@Nullable Bundle savedInstanceState);

    // 用于布局文件view的初始化操作
    public abstract void initView();

    // 用初始化presenter
    public abstract T initPresenter();

    public void addFragment(int fragmentLayoutId, Fragment fragment) {
        if (!fragment.isAdded()) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(fragmentLayoutId, fragment);
            transaction.commit();
        }
    }

    public void removeFragment(int fragmentLayoutId, Fragment fragment) {
        if (fragment.isAdded()) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(fragment);
            transaction.commit();
        }
    }

    public void checkPermission(CheckPermListener listener, String... permissions) {
        mListener = listener;
        XXPermissions.with(mContext)
                .permission(permissions)
                .request(this);
    }

    @Override
    public void hasPermission(List<String> granted, boolean isAll) {
        if (mListener != null && isAll) {
            mListener.agreeAllPermission();//同意了全部权限的回调
        } else {
            mCommonDialog.showDialog("当前应用缺少必要权限。\n请点击\"设置\"-\"权限\"-打开所需权限。"
                    , "设置", "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            XXPermissions.gotoPermissionSettings(mContext);
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mCommonDialog.dismiss();
                        }
                    });
        }
    }

    @Override
    public void noPermission(List<String> denied, boolean quick) {
        mCommonDialog.showDialog("当前应用缺少必要权限。\n请点击\"设置\"-\"权限\"-打开所需权限。"
                , "设置", "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        XXPermissions.gotoPermissionSettings(mContext);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCommonDialog.dismiss();
                    }
                });
    }

    @Override
    public void startActivity(Intent intent) {
        //透传当前Activity的eventBusId
        intent.putExtra(LAST_EVENTBUS_ID, mEventBusId);
        super.startActivity(intent);
        //        overridePendingTransition(R.anim.translate_to_left_in, R.anim.translate_to_left_out);
    }

    public interface CheckPermListener {
        //权限通过后的回调方法
        void agreeAllPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogF.d(TAG, getClass().getSimpleName() + "====onDestroy()");
        if (null != mPresenter)
            mPresenter.detach();
        unregisterReceiver(mCommonReceiver);
        ActivityManager.getInstance().popActivityNotFinish(this);
    }

    private BroadcastReceiver mCommonReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    };

    /**
     * Toast
     *
     * @param toastContent Toast内容
     */
    public void showToast(String toastContent) {
        if (mContext != null && !mContext.isDestroyed()) {
            Toast.makeText(mContext, toastContent, Toast.LENGTH_SHORT).show();
        }
    }
}
