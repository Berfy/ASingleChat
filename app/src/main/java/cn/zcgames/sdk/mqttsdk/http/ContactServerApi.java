package cn.zcgames.sdk.mqttsdk.http;

import android.content.Context;

import java.util.List;

import cn.berfy.sdk.http.HttpApi;
import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.callback.SuperOkHttpCallBack;
import cn.berfy.sdk.http.model.HttpParams;
import cn.berfy.sdk.mvpbase.model.User;
import cn.berfy.sdk.mvpbase.util.GsonUtil;
import cn.zcgames.sdk.mqttsdk.app.MyApplication;
import cn.zcgames.sdk.mqttsdk.contacts.model.ContactBean;
import cn.zcgames.sdk.mqttsdk.contacts.model.SortModel;

public class ContactServerApi {
    private static ContactServerApi contactApi;
    private Context mContext;

    private ContactServerApi(Context context) {
        this.mContext = context;
    }

    public static ContactServerApi getInstance() {
        if (null == contactApi) {
            contactApi = new ContactServerApi(MyApplication.getContext());
        }
        return contactApi;
    }

    /**
     * 获取通讯录列表
     *
     * @param merch_id 渠道id
     * @param id       用户id
     * @param callBack
     */
    public void getContacts(String merch_id, String id, RequestCallBack<ContactBean> callBack) {
        HttpParams params = new HttpParams();
        params.putParam("merch_id", merch_id);
        params.putParam("id", id);
        HttpApi.getInstances().post("app/v1/user/members", params, new SuperOkHttpCallBack<ContactBean>(callBack){});
    }


    //添加好友
    public void addFriend(String merch_id,String host_id,String guest_id, RequestCallBack<String> callBack) {
        HttpParams params = new HttpParams();
        params.putParam("merch_id", merch_id);
        params.putParam("host_id", host_id);
        params.putParam("guest_id", guest_id);
        HttpApi.getInstances().post("app/v1/user/members/add", params, new SuperOkHttpCallBack<>(callBack));
    }

    //搜索好友
    public void searchFriend(String nickname, String pg, RequestCallBack<ContactBean> callBack) {
        HttpParams params = new HttpParams();
        params.putParam("nm", nickname);
        params.putParam("pg", pg);

        HttpApi.getInstances().get("app/v1/user/members/query", params, new SuperOkHttpCallBack<ContactBean>(callBack) {});
    }
}
