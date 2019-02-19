package cn.zcgames.sdk.mqttsdk.message.presenter;

import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.zcgames.sdk.mqttsdk.http.MessageServerApi;
import cn.zcgames.sdk.mqttsdk.message.iView.IFriendView;
import cn.zcgames.sdk.mqttsdk.message.model.FriendBean;

public class FriendPresenter extends BasePresenter<IFriendView> {
    private MessageServerApi messageApi;

    public FriendPresenter() {
        messageApi = MessageServerApi.getInstance();
    }

    //朋友列表
    public void requestFriendList(String merchId, String userId) {
        messageApi.getFriendList(merchId, userId, new RequestCallBack<FriendBean>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<FriendBean> response) {
                if (200 == response.statusCode) {
                    mView.getFriendsList(true, response.data);
                }
            }

            @Override
            public void onError(NetError error) {

            }
        });
    }

    //接受申请
    public void acceptRequestFriend(String merchId, String requestId, String userId, int acceptStatus, String remark, FriendBean.ContactBean person) {
        messageApi.acceptFriendRequest(merchId, requestId, userId, acceptStatus, remark, new RequestCallBack<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<String> response) {
                if (200 == response.statusCode) {
                    mView.passFriend(true, response.data, person);
                }
            }

            @Override
            public void onError(NetError error) {

            }
        });
    }

}
