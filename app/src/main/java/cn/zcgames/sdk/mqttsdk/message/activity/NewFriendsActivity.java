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
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.contacts.event.UpdateContactEvent;
import cn.zcgames.sdk.mqttsdk.message.adapter.FriendsAdapter;
import cn.zcgames.sdk.mqttsdk.message.iView.IFriendView;
import cn.zcgames.sdk.mqttsdk.message.model.FriendBean;
import cn.zcgames.sdk.mqttsdk.message.presenter.FriendPresenter;
import cn.zcgames.sdk.mqttsdk.message.view.model.FriendPassEvent;
import org.greenrobot.eventbus.EventBus;

public class NewFriendsActivity extends CommonActivity<IFriendView, FriendPresenter> implements IFriendView {
    @BindView(R.id.rv_friends)
    RecyclerView mRVFreiends;

    private LinearLayoutManager manager;
    private List<FriendBean.ContactBean> friendDatas;
    private FriendsAdapter friendsAdapter;

    public static void launch(Context context){
        Intent intent = new Intent(context, NewFriendsActivity.class);
        context.startActivity(intent);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_friend;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        showTitleBar();
        getTitleBar().setTitle(getString(R.string.new_friends));
        getTitleBar().setOnBackListener(view ->{
            finish();
        });
        darkStatusBar(false);

        friendDatas = new ArrayList<>();
        friendsAdapter = new FriendsAdapter(this, friendDatas, mPresenter);
        manager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mRVFreiends.setLayoutManager(manager);
        mRVFreiends.setAdapter(friendsAdapter);

        initListener();
        //新朋友列表
        mPresenter.requestFriendList(MyApplication.getCurrLoginUser().getPlayer().getMerchid(), MyApplication.getCurrLoginUser().getPlayer().getId());
    }

    private void initListener() {

    }

    @Override
    public FriendPresenter initPresenter() {
        return new FriendPresenter();
    }

    //获取好友列表
    @Override
    public void getFriendsList(boolean isOk, FriendBean data) {
        if(isOk){
//            if(data != null && data.getNew_friends() != null && data.getNew_friends().size() > 0){

                friendDatas.addAll(data.getNew_friends());
                friendsAdapter.notifyDataSetChanged();
//            }
        }
    }

    //同意好友请求
    @Override
    public void passFriend(boolean isOk, String data, FriendBean.ContactBean person) {
        if(isOk){
            ToastUtil.getInstances().showShort(R.string.add_friend_success);
            EventBus.getDefault().post(new FriendPassEvent(true, person));
            EventBus.getDefault().post(new UpdateContactEvent(true));
            if(friendDatas != null && friendDatas.size() > 0){
                friendDatas.clear();
            }
            mPresenter.requestFriendList(MyApplication.getCurrLoginUser().getPlayer().getMerchid(), MyApplication.getCurrLoginUser().getPlayer().getId());
        }else{
            ToastUtil.getInstances().showShort(R.string.add_friend_faild);
        }
    }

    @Override
    public void hiddenLoadingView(String msg) {

    }

    @Override
    public void showLoadingView(String msg) {

    }

}
