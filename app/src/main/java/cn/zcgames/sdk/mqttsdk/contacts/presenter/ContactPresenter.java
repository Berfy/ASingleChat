package cn.zcgames.sdk.mqttsdk.contacts.presenter;

import java.util.List;

import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.model.User;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.zcgames.sdk.mqttsdk.contacts.model.ContactBean;
import cn.zcgames.sdk.mqttsdk.contacts.model.SortModel;
import cn.zcgames.sdk.mqttsdk.contacts.view.iView.IContactView;
import cn.zcgames.sdk.mqttsdk.http.ContactServerApi;

public class ContactPresenter extends BasePresenter<IContactView> {
    private ContactServerApi contactApi;

    public ContactPresenter() {
        contactApi = ContactServerApi.getInstance();
    }

    public void getContacts(String merchId, String account, boolean isFirst) {
        contactApi.getContacts(merchId, account, new RequestCallBack<ContactBean>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<ContactBean> response) {
                if (200 == response.statusCode) {
                    mView.getContactData(true, response.data.getContact(), isFirst);
                }
            }

            @Override
            public void onError(NetError error) {
                LogF.d("ContactsFragment", "===" + error.errMsg + "==" + error.statusCode);
                mView.getContactData(false, null, isFirst);
            }
        });
    }


    public void searchFriend(String content,String pg) {
        contactApi.searchFriend(content,pg, new RequestCallBack<ContactBean>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<ContactBean> response) {
                if (200 == response.statusCode) {
                    mView.searchFriend(true, response.data.getPlayers());
                }
            }

            @Override
            public void onError(NetError error) {
                mView.searchFriend(false, null);
            }
        });
    }


    public void addFriend(String merch_id,String host_id,String guest_id, boolean isList) {
        contactApi.addFriend(merch_id,host_id,guest_id, new RequestCallBack<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<String> response) {
                if (200 == response.statusCode) {
                    mView.addFriend(true, guest_id, isList);
                }
            }

            @Override
            public void onError(NetError error) {
                mView.addFriend(false, guest_id, isList);
            }
        });
    }
}
