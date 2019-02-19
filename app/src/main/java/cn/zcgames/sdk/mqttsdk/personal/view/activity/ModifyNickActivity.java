package cn.zcgames.sdk.mqttsdk.personal.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;

import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.zcgames.sdk.mqttsdk.personal.view.iView.IMineView;
import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.contacts.event.UpdateUserInfoEvent;
import cn.zcgames.sdk.mqttsdk.personal.model.PersonInfoBean;
import cn.zcgames.sdk.mqttsdk.personal.presenter.MinePresenter;

public class ModifyNickActivity extends CommonActivity<IMineView, MinePresenter> implements IMineView {

    @BindView(R.id.et_nick)
    EditText mEtNick;
    private String nick;

    public static void luncher(Context context) {
        Intent intent = new Intent(context, ModifyNickActivity.class);
        context.startActivity(intent);
    }


    @Override
    public int getContentViewId() {
        return R.layout.activity_modify_nick_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        nick = MyApplication.getCurrLoginUser().getPlayer().getNickname();
    }

    @Override
    public void initView() {
        showTitleBar();
        getTitleBar().setTitle(getString(R.string.modify_nick));
        getTitleBar().setRightTitle(true, getString(R.string.sure), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //确定
                if (mEtNick.getText().toString().trim().length() > 0) {
                    mPresenter.modifyNick(mEtNick.getText().toString());
                } else {
                    ToastUtil.getInstances().showShort(getString(R.string.empty_nick_hint));
                }
            }
        });

        mEtNick.setText(nick);
    }

    @Override
    public MinePresenter initPresenter() {
        return new MinePresenter();
    }


    @OnClick({R.id.iv_clear})
    public void click(View view) {
        switch (view.getId()) {
            case R.id.iv_clear://清除
                mEtNick.setText("");
                break;
        }
    }

    @Override
    public void modifyNick(boolean isOk) {
        if (isOk) {
            //更新数据
            EventBus.getDefault().post(new UpdateUserInfoEvent(true));
            ToastUtil.getInstances().showShort(getString(R.string.modify_success));
        } else {
            ToastUtil.getInstances().showShort(getString(R.string.modify_failure));
        }
        finish();
    }

    @Override
    public void modifyAvatar(boolean isOk) {
    }

    @Override
    public void getMyInfo(boolean isOk, PersonInfoBean user) {
    }

    @Override
    public void hiddenLoadingView(@org.jetbrains.annotations.Nullable String msg) {

    }

    @Override
    public void showLoadingView(@org.jetbrains.annotations.Nullable String msg) {

    }
}
