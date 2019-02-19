package cn.zcgames.sdk.mqttsdk.personal.presenter;

import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.service.im.manager.IMManager;
import cn.berfy.service.im.model.contact.UserInfo;
import cn.zcgames.sdk.mqttsdk.http.LoginServerApi;
import cn.zcgames.sdk.mqttsdk.personal.model.PersonInfoBean;
import cn.zcgames.sdk.mqttsdk.personal.view.iView.IPersonView;

public class PersonPresenter extends BasePresenter<IPersonView> {
    private LoginServerApi loginApi;

    public PersonPresenter(){
        loginApi = LoginServerApi.getInstance();
    }

    public void requestPersonInfo(String uid){
        IMManager.Companion.getInstance().getContactsManager().getUserInfo(uid, new RequestCallBack<UserInfo>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<UserInfo> response) {
                if(200 == response.statusCode){
                    mView.getPersonInfo(true, response.data);
                }
            }

            @Override
            public void onError(NetError error) {

            }
        });
    }

    public void deleteFriend(String merchId, String hostId, String guestId){
        loginApi.deleteFriend(merchId, hostId, guestId, new RequestCallBack<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<String> response) {
                if(200 == response.statusCode){
                    mView.deleteFriend(true, response.data);
                }
            }

            @Override
            public void onError(NetError error) {

            }
        });
    }
}
