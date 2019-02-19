package cn.zcgames.sdk.mqttsdk.personal.presenter;

import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.model.User;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.service.im.manager.IMManager;
import cn.zcgames.sdk.mqttsdk.http.LoginServerApi;
import cn.zcgames.sdk.mqttsdk.personal.view.iView.IRegisterView;

public class RegisterPresenter extends BasePresenter<IRegisterView> {
    private LoginServerApi loginApi;

    public RegisterPresenter(){
        loginApi = LoginServerApi.getInstance();
    }

    //请求注册接口
    public void requestRegisterApi(String merchId, String account){
        IMManager.Companion.getInstance().login(merchId, account, new RequestCallBack<User>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<User> response) {
                if(200 == response.statusCode){
                    mView.getRegisterData(true, response.data);
                }
            }

            @Override
            public void onError(NetError error) {

            }
        });
    }

}
