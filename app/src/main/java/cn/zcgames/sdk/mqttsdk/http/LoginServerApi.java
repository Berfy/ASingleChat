package cn.zcgames.sdk.mqttsdk.http;

import android.content.Context;
import cn.berfy.sdk.http.HttpApi;
import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.callback.SuperOkHttpCallBack;
import cn.berfy.sdk.http.model.HttpParams;
import cn.berfy.sdk.mvpbase.model.User;
import cn.berfy.sdk.mvpbase.util.GsonUtil;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.personal.model.PersonInfoBean;

public class LoginServerApi {

    private static LoginServerApi loginApi;
    private Context mContext;

    private LoginServerApi(Context context) {
        this.mContext = context;
    }

    public static LoginServerApi getInstance() {
        if (null == loginApi) {
            loginApi = new LoginServerApi(MyApplication.getContext());
        }
        return loginApi;
    }

    //设置备注接口
    public void setLabels(String uid, String note, RequestCallBack<String> callBack) {
        HttpParams params = new HttpParams();
        params.putParam("uid", uid);
        params.putParam("remark", note);
        HttpApi.getInstances().post("app/v1/user/bkname", params, new SuperOkHttpCallBack<String>(callBack){});
    }

    //删除好友"merch_id": "渠道id",
    //  "host_id": "主动发起请求的玩家id",
    //  "guest_id": "被动接收请求的玩家id"
    public void deleteFriend(String merchId, String hostId, String guestId, RequestCallBack<String> callBack) {
        HttpParams params = new HttpParams();
        params.putParam("merch_id",merchId);
        params.putParam("host_id",hostId);
        params.putParam("guest_id",guestId);
        HttpApi.getInstances().post("app/v1/user/members/remove", params, new SuperOkHttpCallBack<>(callBack));
    }

}
