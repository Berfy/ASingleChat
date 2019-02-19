package cn.polaris.mqttuikit.presenter;

import android.support.v7.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.berfy.service.im.model.MessageCustom;
import cn.berfy.service.im.model.MessageSystem;
import cn.polaris.mqttuikit.adapter.MessageSysAdapter;
import cn.polaris.mqttuikit.iview.IMessageSysView;

/**
 * 系统消息presener
 *
 * @author NorthStar
 * @date 2019/1/3 15:59
 */
public class MessageSysPresenter extends BasePresenter<IMessageSysView> {
    private final String TAG = "系统通知";
    private MessageSysAdapter mAdapter;
    List<MessageSystem> sysMessages = new ArrayList<>();

    public void initView() {
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        mAdapter = new MessageSysAdapter(mContext, (sysMessage, position) -> {
            if (sysMessage.is_jump()) {
                ToastUtil.getInstances().showShort("点击了第 " + position + " 条系统消息");
            }
        });
        mView.getRecyclerView().setLayoutManager(manager);
        mView.getRecyclerView().setAdapter(mAdapter);
        mView.showLoadingView("");
        getData(true);
    }

    public void getData(boolean isCross) {
        MessageSystem sysMessage = new MessageSystem();
        sysMessage.setTime(System.currentTimeMillis());
        sysMessage.setSys_msg("心悦向您发起了约会邀请");
        for (int i = 0; i < 10; i++) {
            sysMessages.add(sysMessage);
        }
        updateData(isCross, sysMessages);
    }


    private void updateData(boolean isCross, List<MessageSystem> sysMessages) {
        if (!isCross) {
            mAdapter.clear();
        }
        mAdapter.addAll(sysMessages);
        notifyAdapter();
        mAdapter.updateShowTime();
    }

    private void notifyAdapter() {
        if (null == mView.getRecyclerView().getAdapter()) {
            mView.getRecyclerView().setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }
}