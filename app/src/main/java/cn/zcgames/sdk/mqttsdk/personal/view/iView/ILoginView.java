package cn.zcgames.sdk.mqttsdk.personal.view.iView;

import cn.berfy.sdk.mvpbase.iview.IBaseView;
import cn.berfy.sdk.mvpbase.model.User;

public interface ILoginView extends IBaseView {

    void getLoginData(boolean isOk, User data);
}
