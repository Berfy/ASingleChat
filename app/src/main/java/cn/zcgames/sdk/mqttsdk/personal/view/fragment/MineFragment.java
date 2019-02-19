
package cn.zcgames.sdk.mqttsdk.personal.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import cn.zcgames.sdk.mqttsdk.personal.view.iView.IMineView;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.Nullable;

import butterknife.BindView;
import butterknife.OnClick;
import cn.berfy.sdk.mvpbase.base.ActivityManager;
import cn.berfy.sdk.mvpbase.base.BaseApplication;
import cn.berfy.sdk.mvpbase.base.CommonFragment;
import cn.berfy.sdk.mvpbase.model.User;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.view.CircleImageView;
import cn.berfy.service.im.manager.ContactManager;
import cn.berfy.service.im.manager.IMManager;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.contacts.event.UpdateUserInfoEvent;
import cn.zcgames.sdk.mqttsdk.message.activity.IMTestActivity;
import cn.zcgames.sdk.mqttsdk.personal.model.PersonInfoBean;
import cn.zcgames.sdk.mqttsdk.personal.presenter.MinePresenter;
import cn.zcgames.sdk.mqttsdk.personal.view.activity.LoginActivity;
import cn.zcgames.sdk.mqttsdk.personal.view.activity.ModifyAvatorActivity;
import cn.zcgames.sdk.mqttsdk.personal.view.activity.ModifyNickActivity;
import cn.zcgames.sdk.mqttsdk.personal.view.activity.MySconCodeActivity;

/**
 * 我的页面
 */
public class MineFragment extends CommonFragment<IMineView, MinePresenter> implements IMineView {
    public static final String TAG = "MineFragment";

    @BindView(R.id.iv_head)
    CircleImageView mIvHead;
    @BindView(R.id.tv_nick)
    TextView mTvNick;

    @Override
    public void initData(Bundle savedInstanceState) {

    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_main_mine;
    }


    @Override
    public void initView() {
        EventBus.getDefault().register(this);
        //设置标题
        setTitleData();
        setUserData();
    }

    private void setUserData() {
        LogF.d(TAG, "==" + MyApplication.getCurrLoginUser().getPlayer().getNickname() + "==" +
                MyApplication.getCurrLoginUser().getPlayer().getAvatar() + "==" +
                MyApplication.getCurrLoginUser().getPlayer().getId());

        mTvNick.setText(MyApplication.getCurrLoginUser().getPlayer().getNickname());
        Glide.with(getActivity()).load(MyApplication.getCurrLoginUser().getPlayer().getAvatar()).dontAnimate().
                placeholder(R.drawable.placeholder).centerCrop().into(mIvHead);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshUserInfo(UpdateUserInfoEvent event) {
        if (event.isRefresh()) {
            updateMyInfo();//刷新
        }
    }

    //刷新数据
    private void updateMyInfo() {
        mPresenter.getMyInfo();
    }


    @Override
    public MinePresenter initPresenter() {
        return new MinePresenter();
    }


    private void setTitleData() {
        showTitleBar();
        darkStatusBar(false);
        getTitleBar().setLeftIcon(false);
        getTitleBar().setTitleColor(ContextCompat.getColor(mContext, cn.polaris.mqttuikit.R.color.white_normal));
        getTitleBar().setTitle(getString(R.string.home_nav4));
//        getTitleBar().setRightIcon(true, cn.polaris.mqttuikit.R.drawable.add, v -> iMTest());
    }

    //TODO 测试IM代码,之后会删除
    private void iMTest() {
        startActivity(new Intent(getActivity(), IMTestActivity.class));
    }


    @OnClick({R.id.iv_head, R.id.iv_modify_nick, R.id.tv_my_code, R.id.tv_logout})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.iv_head://修改头像
                ModifyAvatorActivity.luncher(getActivity());
                break;
            case R.id.iv_modify_nick://修改昵称
                ModifyNickActivity.luncher(getActivity());
                break;
            case R.id.tv_my_code://我的二维码
                MySconCodeActivity.luncher(getActivity());
                break;
            case R.id.tv_logout://退出登录 留着测试用
                if (IMManager.Companion.isInstanced()) {
                    IMManager.Companion.getInstance().stopConnect();
                }
                if(ContactManager.Companion.getInstance()!=null){
                    ContactManager.Companion.getInstance().deleteList();
                }
                MyApplication.updateCurrLoginUser(null);
                ActivityManager.getInstance().popAllActivity();
                startActivity(new Intent(mContext, LoginActivity.class));
                break;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void modifyNick(boolean isOk) {
    }

    @Override
    public void modifyAvatar(boolean isOk) {
    }

    @Override
    public void getMyInfo(boolean isOk, PersonInfoBean user) {
        if (isOk) {
            mTvNick.setText(user.getNickname());
            Glide.with(getActivity()).load(user.getAvatar()).dontAnimate().
                    placeholder(R.drawable.placeholder).centerCrop().into(mIvHead);

            LogF.d(TAG,"==="+user.getAvatar()+"=="+user.getNickname());
            User userInfo = MyApplication.getCurrLoginUser();
            User.PlayerBean player = userInfo.getPlayer();
            player.setId(user.getUid());
            player.setAvatar(user.getAvatar());
            player.setNickname(user.getNickname());
            player.setQrcode(user.getQrcode());
            userInfo.setPlayer(player);
            BaseApplication.updateCurrLoginUser(userInfo);
//            setUserData();
        } else {
            LogF.d(TAG, "===getInfo-failure");
        }
    }


    @Override
    public void hiddenLoadingView(@Nullable String msg) {
    }

    @Override
    public void showLoadingView(@Nullable String msg) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
