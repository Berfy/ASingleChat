package cn.zcgames.sdk.mqttsdk.personal.view.iView;

import cn.berfy.sdk.mvpbase.iview.IBaseView;
import cn.berfy.sdk.mvpbase.model.User;

public interface IRegisterView extends IBaseView {

    void getRegisterData(boolean isOk, User data);
}
