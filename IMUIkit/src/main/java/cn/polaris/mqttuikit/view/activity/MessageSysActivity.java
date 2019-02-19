package cn.polaris.mqttuikit.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.util.AnimUtil;
import cn.berfy.service.im.model.MessageCustom;
import cn.berfy.service.im.model.MessageSystem;
import cn.polaris.mqttuikit.R;
import cn.polaris.mqttuikit.iview.IMessageSysView;
import cn.polaris.mqttuikit.presenter.MessageSysPresenter;


/**
 * 系统通知
 * @author NorthStar
 * @date  2019/1/3 15:59
 */
public class MessageSysActivity extends CommonActivity<IMessageSysView, MessageSysPresenter> implements IMessageSysView {
    RecyclerView mRefreshableView;
    public static void launcher(Context context) {
        Intent intent = new Intent(context, MessageSysActivity.class);
        AnimUtil.jump2NextPage(context, intent);
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_message_sys;
    }

    @Override
    public void initView() {
        mRefreshableView = findViewById(R.id.refreshView);
        setTitleBar();
        getPresenter().initView();
    }

    private void setTitleBar() {
        showTitleBar();
        getTitleBar().setTitle(R.string.message_sys);
        getTitleBar().setOnBackListener(v -> finish());
    }

    @Override
    public MessageSysPresenter initPresenter() {
        return new MessageSysPresenter();
    }

    @Override
    public RecyclerView getRecyclerView() {
        return mRefreshableView;
    }

    @Override
    public void getApplyInviteResult(MessageSystem sysMessage, int position, boolean isAccept) {

    }

    @Override
    public void hiddenLoadingView(String msg) {

    }

    @Override
    public void showLoadingView(String msg) {

    }
}
