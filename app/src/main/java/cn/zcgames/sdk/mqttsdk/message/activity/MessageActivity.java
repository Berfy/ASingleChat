package cn.zcgames.sdk.mqttsdk.message.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.iview.IBaseView;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.AnimUtil;
import cn.polaris.mqttuikit.view.fragment.MessageFragment;
import cn.zcgames.sdk.im.R;

/**
 * 消息列表界面
 *
 * @author NorthStar
 * @date 2018/12/28 17:54
 */
public class MessageActivity extends CommonActivity<IBaseView, BasePresenter<IBaseView>> implements IBaseView {
    public static final String TAG = "MessageActivity";
    private MessageFragment mMessageFragment;

    public static void launcher(Context context, String name, String password) {
        Intent intent = new Intent(context, MessageActivity.class);
        AnimUtil.jump2NextPage(context, intent);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_message;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
    }

    @Override
    public void initView() {
        setTitleBar();
        setFragment();
    }

    private void setTitleBar() {
        showTitleBar();
        getTitleBar().setLeftIcon(true, v -> finish());
        getTitleBar().setTitle("消息列表");
    }


    private void setFragment() {
        mMessageFragment = MessageFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.layout_content, mMessageFragment).show(mMessageFragment).commit();
    }

    @Override
    public BasePresenter initPresenter() {
        return null;
    }

    @Override
    public void hiddenLoadingView(@org.jetbrains.annotations.Nullable String msg) {

    }

    @Override
    public void showLoadingView(@org.jetbrains.annotations.Nullable String msg) {

    }
}
