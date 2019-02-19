package cn.zcgames.sdk.mqttsdk.message.iView;

import cn.berfy.sdk.mvpbase.iview.IBaseView;
import cn.zcgames.sdk.mqttsdk.message.model.GroupBean;

public interface IGroupListView extends IBaseView{

    void getGroupList(boolean isOk, GroupBean data);

}
