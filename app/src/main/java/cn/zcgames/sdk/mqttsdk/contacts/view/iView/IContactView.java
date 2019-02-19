package cn.zcgames.sdk.mqttsdk.contacts.view.iView;

import java.util.List;

import cn.berfy.sdk.mvpbase.iview.IBaseView;
import cn.zcgames.sdk.mqttsdk.contacts.model.SortModel;

public interface IContactView extends IBaseView {
    void getContactData(boolean isOk, List<SortModel> data, boolean isFirst);

    void addFriend(boolean isOk,String uid, boolean isList);

    void searchFriend(boolean isOk,List<SortModel> data);
}
