package cn.zcgames.sdk.mqttsdk.personal.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.berfy.sdk.mvpbase.base.ActivityManager;
import cn.berfy.sdk.mvpbase.base.BaseApplication;
import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.model.User;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.home.view.activity.MainActivity;
import cn.zcgames.sdk.mqttsdk.personal.presenter.RegisterPresenter;
import cn.zcgames.sdk.mqttsdk.personal.view.iView.IRegisterView;

public class RegisterActivity extends CommonActivity<IRegisterView, RegisterPresenter> implements IRegisterView {
    @BindView(R.id.et_account_num)
    EditText mETAccount;
    @BindView(R.id.et_pwd_num)
    EditText mETPwd;
    @BindView(R.id.tv_register)
    TextView mTVRegister;
    @BindView(R.id.tv_go_login)
    TextView mTVGoLogin;

    public static void launch(Context context){
        Intent intent = new Intent(context, RegisterActivity.class);
        context.startActivity(intent);
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_register;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        showTitleBar();
        getTitleBar().setTitle(getString(R.string.register));
        getTitleBar().setOnBackListener(view -> {
            finish();
        });
        darkStatusBar(false);

        setUnderLineString(mTVGoLogin, getString(R.string.go_to_login));
    }

    @Override
    public RegisterPresenter initPresenter() {
        return new RegisterPresenter();
    }

    @Override
    public void getRegisterData(boolean isOk, User data) {
        if(isOk){
            if(null != data){
                BaseApplication.updateCurrLoginUser(data);
                startMainActivity();
            }
        }
    }

    @Override
    public void hiddenLoadingView(String msg) {

    }

    @Override
    public void showLoadingView(String msg) {

    }

    @OnClick({R.id.tv_register, R.id.tv_go_login})
    public void onClickView(View view){
        switch (view.getId()){
            case R.id.tv_register:
                String username = mETAccount.getText().toString().trim();
               if(TextUtils.isEmpty(username)){
                    ToastUtil.getInstances().showShort(getString(R.string.please_input_account));
                    return;
                }
                 /*if(TextUtils.isEmpty(mETPwd.getText().toString())){
                    ToastUtil.getInstances().showShort(getString(R.string.please_input_pwd));
                    return;
                }*/
                mPresenter.requestRegisterApi("1234", username);
                break;
            case R.id.tv_go_login:
                finish();
                break;
        }

    }

    //设置下划线
    public void setUnderLineString(TextView tv, String content){
        SpannableString spannableString = new SpannableString(content);
        spannableString.setSpan(new UnderlineSpan(), 0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(spannableString);
    }

    public void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        ActivityManager.getInstance().popAllActivity();
        startActivity(intent);
        finish();
    }
}
