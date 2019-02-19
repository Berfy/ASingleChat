package cn.zcgames.sdk.mqttsdk.personal.presenter;

import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.zcgames.sdk.mqttsdk.http.MineServerApi;
import cn.zcgames.sdk.mqttsdk.personal.model.PersonInfoBean;
import cn.zcgames.sdk.mqttsdk.personal.view.iView.IMineView;

public class MinePresenter extends BasePresenter<IMineView> {
    private static final String TAG="MinePresenter";
    private MineServerApi mineApi;

    public MinePresenter() {
        mineApi = MineServerApi.getInstance();
    }


    public void getMyInfo(){
       mineApi.getMyInfo(new RequestCallBack<PersonInfoBean>() {
           @Override
           public void onStart() {

           }

           @Override
           public void onFinish(NetResponse<PersonInfoBean> response) {
                mView.getMyInfo(response.isOk(),response.data);
           }

           @Override
           public void onError(NetError error) {

           }
       });
    }


    public void modifyNick(String nick){
        mineApi.modifyNick(nick, new RequestCallBack<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<String> response) {
                mView.modifyNick(response.isOk());
            }

            @Override
            public void onError(NetError error) {
                mView.modifyNick(false);
                LogF.d(TAG,"==="+error.statusCode+"=="+error.errMsg);
            }
        });
    }

    public void modifyAvatar(String avatar){
        mineApi.modifyAvatar(avatar, new RequestCallBack<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<String> response) {
                mView.modifyAvatar(response.isOk());
            }

            @Override
            public void onError(NetError error) {
                mView.modifyAvatar(false);
                LogF.d(TAG,"==="+error.statusCode+"=="+error.errMsg);
            }
        });
    }
}
