package cn.zcgames.sdk.mqttsdk.personal.presenter;

import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.zcgames.sdk.mqttsdk.http.LoginServerApi;
import cn.zcgames.sdk.mqttsdk.personal.view.iView.ILabelsView;

public class LabelsPresenter extends BasePresenter<ILabelsView> {
    private LoginServerApi loginApi;

    public LabelsPresenter(){
        loginApi = LoginServerApi.getInstance();
    }

    //设置标签
    public void perosnSetLabels(String uid, String note){
        loginApi.setLabels(uid, note, new RequestCallBack<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<String> response) {
                if(200 == response.statusCode){
                    mView.setLabelsData(true, response.data);
                }
            }

            @Override
            public void onError(NetError error) {

            }
        });
    }

}
