package cn.zcgames.sdk.mqttsdk.message.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.message.adapter.GroupChatAdapter;
import cn.zcgames.sdk.mqttsdk.message.iView.IGroupListView;
import cn.zcgames.sdk.mqttsdk.message.model.GroupBean;
import cn.zcgames.sdk.mqttsdk.message.presenter.GroupListPresenter;

public class GroupListActivity extends CommonActivity<IGroupListView, GroupListPresenter> implements IGroupListView {
    @BindView(R.id.rv_group)
    RecyclerView mRVGroup;

    private LinearLayoutManager manager;
    private List<GroupBean.ListBean> groupDatas;
    private GroupChatAdapter groupAdapter;

    public static void launch(Context context) {
        Intent intent = new Intent(context, GroupListActivity.class);
        context.startActivity(intent);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_group_list;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        showTitleBar();
        getTitleBar().setTitle(getString(R.string.group_chat));
        getTitleBar().setOnBackListener(view -> {
            finish();
        });
        darkStatusBar(false);

        groupDatas = new ArrayList<>();
        groupAdapter = new GroupChatAdapter(this, groupDatas);
        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRVGroup.setLayoutManager(manager);
        mRVGroup.setAdapter(groupAdapter);

        initListener();
        //获取群聊天室
        if (null != MyApplication.getCurrLoginUser())
            mPresenter.requestGroupList(MyApplication.getCurrLoginUser().getPlayer().getMerchid(), null,null);
    }

    private void initListener() {


    }

    @Override
    public GroupListPresenter initPresenter() {
        return new GroupListPresenter();
    }

    @Override
    public void hiddenLoadingView(String msg) {

    }

    @Override
    public void showLoadingView(String msg) {

    }

    @Override
    public void getGroupList(boolean isOk, GroupBean data) {
        if (isOk) {
            if(data != null && data.getList() != null && data.getList().size() > 0){
                groupDatas.addAll(data.getList());
                groupAdapter.notifyDataSetChanged();
            }
        }
    }
}
