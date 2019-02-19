package cn.polaris.mqttuikit.iview;

import android.support.v7.widget.RecyclerView;

import cn.berfy.sdk.mvpbase.iview.IBaseView;
import cn.berfy.service.im.model.MessageCustom;
import cn.berfy.service.im.model.MessageSystem;

/**
 * 系统通知View接口类
 * @author NorthStar
 * @date  2019/1/4 10:47
 */
public interface IMessageSysView extends IBaseView {
    RecyclerView getRecyclerView();
    //是否接受邀请
    void getApplyInviteResult(MessageSystem sysMessage, int position, boolean isAccept);
}
