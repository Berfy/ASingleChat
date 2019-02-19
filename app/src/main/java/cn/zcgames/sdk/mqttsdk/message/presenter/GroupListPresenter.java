package cn.zcgames.sdk.mqttsdk.message.presenter;

import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.zcgames.sdk.mqttsdk.http.MessageServerApi;
import cn.zcgames.sdk.mqttsdk.message.iView.IGroupListView;
import cn.zcgames.sdk.mqttsdk.message.model.GroupBean;

public class GroupListPresenter extends BasePresenter<IGroupListView> {
    private MessageServerApi messageApi;

    public GroupListPresenter() {
        messageApi = MessageServerApi.getInstance();
    }

    //群组列表
    public void requestGroupList(String merchId, String adminId, String playerId) {
        messageApi.getGroupList(merchId, adminId, playerId, new RequestCallBack<GroupBean>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<GroupBean> response) {
                if (200 == response.statusCode) {
                    mView.getGroupList(true, response.data);
                }
            }

            @Override
            public void onError(NetError error) {

            }
        });
    }

}
