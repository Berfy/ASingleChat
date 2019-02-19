package cn.zcgames.sdk.mqttsdk.message.iView;

import cn.berfy.sdk.mvpbase.iview.IBaseView;
import cn.zcgames.sdk.mqttsdk.message.model.FriendBean;

public interface IFriendView extends IBaseView {

    void getFriendsList(boolean isOk, FriendBean data);

    void passFriend(boolean isOk, String data, FriendBean.ContactBean person);
}
