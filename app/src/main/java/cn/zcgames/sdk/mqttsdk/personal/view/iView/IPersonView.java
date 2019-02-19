package cn.zcgames.sdk.mqttsdk.personal.view.iView;

import cn.berfy.sdk.mvpbase.iview.IBaseView;
import cn.berfy.service.im.model.contact.UserInfo;
import cn.zcgames.sdk.mqttsdk.personal.model.PersonInfoBean;

public interface IPersonView extends IBaseView {

    void getPersonInfo(boolean isOk, UserInfo data);

    void deleteFriend(boolean isOk, String data);
}
