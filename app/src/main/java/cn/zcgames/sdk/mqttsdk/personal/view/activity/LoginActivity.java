package cn.zcgames.sdk.mqttsdk.personal.view.activity;

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
import cn.berfy.sdk.mvpbase.base.BaseApplication;
import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.model.User;
import cn.berfy.sdk.mvpbase.util.GsonUtil;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.home.view.activity.MainActivity;
import cn.zcgames.sdk.mqttsdk.personal.presenter.LoginPresenter;
import cn.zcgames.sdk.mqttsdk.personal.view.iView.ILoginView;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;
import java.util.List;

public class LoginActivity extends CommonActivity<ILoginView, LoginPresenter> implements ILoginView {

    @BindView(R.id.et_login_account)
    EditText mETAccount;
    @BindView(R.id.et_login_pwd)
    EditText mETPwd;
    @BindView(R.id.tv_login)
    TextView mTVLogin;
    @BindView(R.id.tv_go_register)
    TextView mTVGoRegister;

    @Override
    public int getContentViewId() {
        return R.layout.activity_login;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        showTitleBar();
        getTitleBar().setLeftIcon(false);
        getTitleBar().setTitle(getString(R.string.login));
        darkStatusBar(false);

        setUnderLineString(mTVGoRegister, getString(R.string.go_to_register));
        checkPermission();
    }

    private void checkPermission() {
        XXPermissions.with(this)
                .permission(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , android.Manifest.permission.READ_PHONE_STATE
                ).request(new OnPermission() {
            @Override
            public void hasPermission(List<String> granted, boolean isAll) {

            }

            @Override
            public void noPermission(List<String> denied, boolean quick) {
                ToastUtil.getInstances().showShort("你拒绝了" + GsonUtil.getInstance().toJson(denied) + "权限");
                XXPermissions.gotoPermissionSettings(mContext);
            }
        });
    }

    @Override
    public LoginPresenter initPresenter() {
        return new LoginPresenter();
    }

    @Override
    public void getLoginData(boolean isOk, User data) {
        if (isOk) {
            if (null != data) {
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

    @OnClick({R.id.tv_login, R.id.tv_go_register})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.tv_login:
                String merchid = mETAccount.getText().toString().trim();
                if (TextUtils.isEmpty(merchid)) {
                    ToastUtil.getInstances().showShort("渠道不能为空");
                    return;
                }
                String extId = mETPwd.getText().toString().trim();
                if (TextUtils.isEmpty(extId)) {
                    ToastUtil.getInstances().showShort("外部id不能为空");
                    return;
                }
                mPresenter.login(merchid, extId);
                break;
            case R.id.tv_go_register:
                RegisterActivity.launch(this);
                break;
        }
    }

    //设置下划线
    public void setUnderLineString(TextView tv, String content) {
        SpannableString spannableString = new SpannableString(content);
        spannableString.setSpan(new UnderlineSpan(), 0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(spannableString);
    }

    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
//        ActivityManager.getInstance().popAllActivity();
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.getLoginStatus();
    }

}
