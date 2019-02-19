package cn.zcgames.sdk.mqttsdk.personal.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.service.im.model.contact.UserInfo;
import cn.zcgames.sdk.mqttsdk.personal.view.iView.IPersonView;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.berfy.sdk.mvpbase.model.User;
import cn.berfy.sdk.mvpbase.view.CircleImageView;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.contacts.event.UpdateContactEvent;
import cn.zcgames.sdk.mqttsdk.message.activity.ChatActivity;
import cn.zcgames.sdk.mqttsdk.personal.model.PersonInfoBean;
import cn.zcgames.sdk.mqttsdk.personal.presenter.PersonPresenter;

public class PersonInfoActivity extends CommonActivity<IPersonView, PersonPresenter> implements IPersonView {
    @BindView(R.id.iv_circle_head)
    CircleImageView mIVHead;
    @BindView(R.id.tv_nick_text)
    TextView mTVNick;
    @BindView(R.id.tv_delete_friend)
    TextView mTVDelete;
    @BindView(R.id.tv_send_message)
    TextView mTVSendMsg;
    @BindView(R.id.tv_note)
    TextView mTVNote;
    private String uId;
    private String nickName;
    private final int REQUEST_CODE = 1001;

    public static void launch(Context context, String uid){
        Intent intent = new Intent(context, PersonInfoActivity.class);
        intent.putExtra("uid", uid);
        context.startActivity(intent);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_person_info;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        showTitleBar();
        getTitleBar().setOnBackListener(view ->{
            finish();
        });
        getTitleBar().setTitle(getString(R.string.person_info));
        darkStatusBar(false);

        uId = getIntent().getStringExtra("uid");

        mPresenter.requestPersonInfo(uId);
    }

    @Override
    public PersonPresenter initPresenter() {
        return new PersonPresenter();
    }

    @Override
    public void getPersonInfo(boolean isOk, UserInfo data) {
        if(isOk){
            Glide.with(mContext).load(data.getAvatar()).asBitmap()
                    .placeholder(R.drawable.placeholder)
                    .into(mIVHead);
            nickName = data.getNickname();
            mTVNick.setText(data.getNickname());
            mTVNote.setText(data.getNickname());
        }
    }

    //删除好友
    @Override
    public void deleteFriend(boolean isOk, String data) {
        if(isOk){
            Log.d("111111","删除好友成功");
            EventBus.getDefault().post(new UpdateContactEvent(true));
            finish();
        }
    }

    @Override
    public void hiddenLoadingView(String msg) {

    }

    @Override
    public void showLoadingView(String msg) {

    }

    @OnClick({R.id.ll_note, R.id.tv_send_message, R.id.tv_delete_friend})
    public void onClickView(View view){
        switch (view.getId()){
            case R.id.ll_note:
                Intent intent = new Intent(this, LabelsActivity.class);
                intent.putExtra("id", uId);
                intent.putExtra("note", mTVNote.getText().toString());
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.tv_send_message:
                ChatActivity.launch(this,0, uId, nickName);
                break;
            case R.id.tv_delete_friend:
                User user = MyApplication.getCurrLoginUser();
                mPresenter.deleteFriend(user.getPlayer().getMerchid(), user.getPlayer().getId(), uId);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(0 == resultCode){
            String rename = data.getStringExtra("reName");
            mTVNote.setText(rename);
        }
    }

}
