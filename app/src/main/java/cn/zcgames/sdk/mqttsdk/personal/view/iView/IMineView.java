package cn.zcgames.sdk.mqttsdk.personal.view.iView;

import cn.berfy.sdk.mvpbase.iview.IBaseView;
import cn.berfy.sdk.mvpbase.model.User;
import cn.zcgames.sdk.mqttsdk.personal.model.PersonInfoBean;

public interface IMineView extends IBaseView{
    void modifyNick(boolean isOk);

    void modifyAvatar(boolean isOk);

    void getMyInfo(boolean isOk, PersonInfoBean user);
}
