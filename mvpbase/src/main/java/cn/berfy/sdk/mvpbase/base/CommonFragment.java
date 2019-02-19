package cn.berfy.sdk.mvpbase.base;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;

import java.util.List;
import java.util.Objects;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.berfy.sdk.mvpbase.R;
import cn.berfy.sdk.mvpbase.iview.IBaseView;
import cn.berfy.sdk.mvpbase.listener.CommonFragmentListener;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.view.TitleBarView;
import cn.berfy.sdk.mvpbase.view.dialog.CommonDialog;

/**
 * Fragment的基类
 *
 * @author Berfy
 */
public abstract class CommonFragment<V extends IBaseView, T extends BasePresenter<V>> extends Fragment implements OnPermission {

    private static final String TAG = "CommonFragment";
    private int mSystemUiVisibility = -1;
    protected T mPresenter;
    //定向传递需要双方保持id一致，即接收和发送方一致，不判断id一致则群发
    protected long mEventBusId;//本次id、用于定向传递给下一个Activitry发生关系
    protected long mPreEventBusId;//被传递过来的Id，用于定向给上一个Activity发生关系
    private final String LAST_EVENTBUS_ID = "last_eventbus_id";
    protected View mRootView;
    private Unbinder mUnbinder;
    private LinearLayout mLayoutContent;//界面容器
    protected Context mContext;
    protected TitleBarView mTitleBarView;
    private CommonFragmentListener mCommonFragmentListener;
    protected CommonDialog mCommonDialog;
    private CommonActivity.CheckPermListener mListener; //权限回调接口

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (null != mCommonFragmentListener) {
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

    @Override
    public void onResume() {
        super.onResume();
        if (null != mCommonFragmentListener) {
            mCommonFragmentListener.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mCommonFragmentListener) {
            mCommonFragmentListener.onResume();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.base_fragment, container, false);
            //缓存的rootView需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个rootView已经有pa.rent的错误。
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (parent != null) {
                parent.removeView(mRootView);
            }
            mLayoutContent = mRootView.findViewById(R.id.common_layout_content);
            mLayoutContent.addView(inflater.inflate(getContentViewId(), null), mLayoutContent.getLayoutParams());
            mTitleBarView = mRootView.findViewById(R.id.common_titlebarview);
            mUnbinder = ButterKnife.bind(this, mRootView);
        }
        if (null != mCommonFragmentListener) {
            mCommonFragmentListener.onCreateView(inflater, container, savedInstanceState);
        }
        return mRootView;
    }

    protected void showTitleBar() {
        mTitleBarView.setVisibility(View.VISIBLE);
    }

    protected TitleBarView getTitleBar() {
        return mTitleBarView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mTitleBarView.setVisibility(View.GONE);
        mTitleBarView.setLeftIcon(false);
        mCommonDialog = new CommonDialog(getActivity());
        mPresenter = initPresenter();
        if (null != mPresenter)
            mPresenter.attach(mContext, (V) this);
        initView();
        if (null != mCommonFragmentListener) {
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
    public void startActivity(Intent intent) {
        //透传当前Activity的eventBusId
        intent.putExtra(LAST_EVENTBUS_ID, mEventBusId);
        super.startActivity(intent);
//        getActivity().overridePendingTransition(R.anim.translate_to_left_in, R.anim.translate_to_left_out);
    }

    // 获取布局文件用于加载布局
    public abstract int getContentViewId();

    // 加载数据
    public abstract void initData(Bundle savedInstanceState);

    // 加载控件
    public abstract void initView();

    // 用初始化presenter
    public abstract T initPresenter();

    public T getPresenter() {
        return mPresenter;
    }

    public void addFragment(int fragmentLayoutId, Fragment fragment) {
        if (!fragment.isAdded()) {
            FragmentManager manager = getChildFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(fragmentLayoutId, fragment);
            transaction.commit();
        }
    }

    public void removeFragment(Fragment fragment) {
        if (fragment.isAdded()) {
            FragmentManager manager = getChildFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(fragment);
            transaction.commit();
        }
    }

    public void removeParentFragment(Fragment fragment) {
        if (fragment.isAdded()) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(fragment);
            transaction.commit();
            if (null != mCommonFragmentListener) {
                mCommonFragmentListener.onFragmentRemoved();
            }
        }
    }

    public void checkPermission(CommonActivity.CheckPermListener listener, String... permissions) {
        mListener = listener;
        XXPermissions.with((Activity) mContext)
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

    /**
     * 状态栏黑色设置
     */
    public void darkStatusBar(boolean isDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = Objects.requireNonNull(getActivity()).getWindow().getDecorView();
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

    public void gotoSettingPermission() {
        //跳转到设置页面
        XXPermissions.gotoPermissionSettings(mContext);
    }

    public boolean hasPermission(List<String> permissions) {
        if (null == permissions) {
            return false;
        }
        String[] sPermissions = new String[permissions.size()];
        return XXPermissions.isHasPermission(getActivity(), permissions.toArray(sPermissions));
    }

    public boolean hasPermission(String permission) {
        if (null == permission) {
            return false;
        }
        return XXPermissions.isHasPermission(getActivity(), permission);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mUnbinder) {
            mUnbinder.unbind();
        }
        if (null != mCommonFragmentListener) {
            mCommonFragmentListener.onDestroyView();
        }
        if (null != mPresenter)
            mPresenter.detach();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (null != mCommonFragmentListener) {
            mCommonFragmentListener.onStop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mCommonFragmentListener) {
            mCommonFragmentListener.onDestroy();
        }
    }
}
