package cn.zcgames.sdk.mqttsdk.http;

import android.content.Context;

import cn.berfy.sdk.http.HttpApi;
import cn.berfy.sdk.http.callback.HttpUploadCallBack;
import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.callback.SuperOkHttpCallBack;
import cn.berfy.sdk.http.model.HttpParams;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.model.User;
import cn.berfy.sdk.mvpbase.util.GsonUtil;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.personal.model.PersonInfoBean;

public class MineServerApi {
    private static final String TAG = "MineServerApi";
    private static MineServerApi mineApi;
    private Context mContext;

    private MineServerApi(Context context) {
        this.mContext = context;
    }

    public static MineServerApi getInstance() {
        if (null == mineApi) {
            mineApi = new MineServerApi(MyApplication.getContext());
        }
        return mineApi;
    }


    //我的信息
    public void getMyInfo(RequestCallBack<PersonInfoBean> callBack){
        HttpParams params = new HttpParams();

        HttpApi.getInstances().get("app/v1/user/mine/info", params, new SuperOkHttpCallBack<PersonInfoBean>(callBack){});
    }


    //修改昵称
    public void modifyNick(String nickname, RequestCallBack<String> callBack) {
        HttpParams params = new HttpParams();
//        params.putParam("uid",MyApplication.getCurrLoginUser().getPlayer().getId());
        params.putParam("avatar",MyApplication.getCurrLoginUser().getPlayer().getAvatar());
        params.putParam("nickname", nickname);
        HttpApi.getInstances().post("app/v1/user/mine/up", params, new SuperOkHttpCallBack<>(callBack));
    }


    //修改头像
    public void modifyAvatar(String avatarUrl, RequestCallBack<String> callBack) {
        HttpParams params = new HttpParams();
//        params.putParam("uid",MyApplication.getCurrLoginUser().getPlayer().getId());
        params.putParam("avatar",avatarUrl);
        params.putParam("nickname", MyApplication.getCurrLoginUser().getPlayer().getNickname());
        HttpApi.getInstances().post("app/v1/user/mine/up", params, new SuperOkHttpCallBack<>(callBack));
    }
}
