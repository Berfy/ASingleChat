package cn.zcgames.sdk.mqttsdk.personal.presenter;

import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.base.BaseApplication;
import cn.berfy.sdk.mvpbase.model.User;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.service.im.manager.IMManager;
import cn.zcgames.sdk.mqttsdk.http.LoginServerApi;
import cn.zcgames.sdk.mqttsdk.personal.view.iView.ILoginView;

public class LoginPresenter extends BasePresenter<ILoginView> {
    private LoginServerApi loginApi;

    public LoginPresenter(){
        loginApi = LoginServerApi.getInstance();
    }

    public void login(String merchId, String account){
        IMManager.Companion.getInstance().login(merchId, account, new RequestCallBack<User>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<User> response) {
                if(200 == response.statusCode){
                    mView.getLoginData(true, response.data);
                }
            }

            @Override
            public void onError(NetError error) {

            }
        });
    }

    /**
     * 获取用户的登录状态
     */
    public void getLoginStatus() {
        boolean isLogin = BaseApplication.getCurrLoginStatus();
        User user = null;
        if (isLogin) {
            user = BaseApplication.getCurrLoginUser();
        }
        if (user != null)
            mView.getLoginData(isLogin, user);
    }

}
