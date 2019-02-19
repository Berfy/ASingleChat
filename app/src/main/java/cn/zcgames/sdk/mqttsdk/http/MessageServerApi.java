package cn.zcgames.sdk.mqttsdk.http;

import android.content.Context;

import cn.berfy.sdk.http.HttpApi;
import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.callback.SuperOkHttpCallBack;
import cn.berfy.sdk.http.model.HttpParams;
import cn.berfy.sdk.mvpbase.util.GsonUtil;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.message.model.FriendBean;
import cn.zcgames.sdk.mqttsdk.message.model.GroupBean;

public class MessageServerApi {
    private static MessageServerApi messageApi;
    private Context mContext;

    private MessageServerApi(Context context) {
        this.mContext = context;
    }

    public static MessageServerApi getInstance() {
        if (null == messageApi) {
            messageApi = new MessageServerApi(MyApplication.getContext());
        }
        return messageApi;
    }

    //获取好友列表
    public void getFriendList(String merchId, String userId, RequestCallBack<FriendBean> callBack) {
        HttpParams params = new HttpParams();
        params.putParam("merch_id", merchId);
        params.putParam("id", userId);
        HttpApi.getInstances().post("app/v1/user/members", params, new SuperOkHttpCallBack<FriendBean>(callBack) {});
    }

    //接受好友申请"merch_id": "渠道id",
    //  "host_id": "主动发起请求的玩家id",
    //  "guest_id": "被动接收请求的玩家id",
    //  "accept": "1:接受，2:拒绝",
    //  "remark": "朋友备注"
    public void acceptFriendRequest(String merchId, String requestId, String userId, int acceptStatus, String remark, RequestCallBack<String> callBack){
        HttpParams params = new HttpParams();
        params.putParam("merch_id", merchId);
        params.putParam("host_id", requestId);
        params.putParam("guest_id", userId);
        params.putParam("accept", acceptStatus);
        params.putParam("remark", remark);
        HttpApi.getInstances().post("app/v1/user/members/accept", params, new SuperOkHttpCallBack<String>(callBack){});
    }

    //获取群列表
    public void getGroupList(String merchId, String adminId, String playerId, RequestCallBack<GroupBean> callBack) {
        HttpParams params = new HttpParams();
        params.putParam("merch_id", merchId);
        params.putParam("admin_id", adminId);
        params.putParam("player_id", playerId);
        HttpApi.getInstances().post("app/v1/group/list", params, new SuperOkHttpCallBack<GroupBean>(callBack) {});
    }
}
