package cn.polaris.mqttuikit.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.berfy.sdk.http.http.okhttp.utils.NetworkUtil;
import cn.berfy.sdk.mvpbase.util.CommonUtil;
import cn.berfy.sdk.mvpbase.util.TimeUtil;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.berfy.sdk.mvpbase.view.RefreshAdapter;
import cn.berfy.service.im.model.MessageCustom;
import cn.berfy.service.im.model.MessageSystem;
import cn.polaris.mqttuikit.R;
import cn.polaris.mqttuikit.holder.EmptyViewHolder;
import cn.polaris.mqttuikit.holder.MessageSysHolder;

/**
 * 系统消息适配器
 *
 * @author NorthStar
 * @date 2019/1/3 15:58
 */
public class MessageSysAdapter extends RefreshAdapter<MessageSystem> {
    private final String TAG = "系统消息";
    private Context mContext;
    private LayoutInflater mInflater;
    private OnMsgClickListener mOnMsgClickListener;
    private static final int EMPTY_VIEW = 1;
    private static final int ITEM_VIEW = 2;
    private long mLastTime;

    public MessageSysAdapter(Context context, OnMsgClickListener onMsgClickListener) {
        super(context, true);
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mOnMsgClickListener = onMsgClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateItem(ViewGroup parent, int viewType) {
        if (viewType == EMPTY_VIEW) {
            return new EmptyViewHolder(mInflater.inflate(R.layout.item_empty_view_holder, parent, false));
        } else {
            return new MessageSysHolder(mInflater.inflate(R.layout.item_message_sys, parent, false));
        }
    }

    @Override
    public void onBindItem(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessageSysHolder) {
            MessageSysHolder messageSysHolder = (MessageSysHolder) holder;
            MessageSystem msg = data.get(position);
            messageSysHolder.tvMsgContent.setText(msg.getSys_msg());

            if (msg.isShowTitleTime()) {
                messageSysHolder.mTvShowTime.setText(TimeUtil.format("yyyy-MM-dd HH:mm:ss", msg.getTime()));
                messageSysHolder.mTvShowTime.setVisibility(View.VISIBLE);
            } else messageSysHolder.mTvShowTime.setVisibility(View.GONE);

            if (msg.is_jump()) {
                holder.itemView.setOnClickListener(view -> {
                    if (null != mOnMsgClickListener) {
                        mOnMsgClickListener.onItemClick(msg, position);
                    }
                });
            }
            //setInviteEvent(messageSysHolder, msg);
        } else if (holder instanceof EmptyViewHolder) {
            EmptyViewHolder emptyViewHolder = (EmptyViewHolder) holder;
            if (NetworkUtil.isNetAvailable(mContext)) {//判断网络状态
                emptyViewHolder.tv.setText(mContext.getString(R.string.message_no_sys));
            } else {
                emptyViewHolder.tv.setText(mContext.getString(R.string.no_net));
            }
        }
    }

    //设置邀请事件
    private void setInviteEvent(MessageSysHolder messageSysHolder, MessageSystem msg) {
        messageSysHolder.bottomLayout.setVisibility(View.VISIBLE);
        messageSysHolder.tvNotifyTitle.setVisibility(View.GONE);
        messageSysHolder.tvNotifyTitle.setText(mContext.getString(R.string.message_sys_type_invite_meet));
        messageSysHolder.tvNotifyTitle.setTextColor(CommonUtil.getColor(R.color.color_bba361));
        messageSysHolder.tvMsgContent.setText(msg.getSys_msg());
        if (msg.isButton_pressed()) {//用户操作过了
            messageSysHolder.tvAccept.setEnabled(false);
            messageSysHolder.tvReject.setEnabled(false);
            messageSysHolder.tvAccept.setVisibility(View.GONE);
            messageSysHolder.tvReject.setVisibility(View.VISIBLE);
            boolean agree = msg.is_agree();
            messageSysHolder.tvReject.setText(agree ? R.string.message_sys_type_agree : R.string.message_sys_type_reject);
        } else {
            messageSysHolder.tvAccept.setEnabled(true);
            messageSysHolder.tvReject.setEnabled(true);
            messageSysHolder.tvAccept.setVisibility(View.VISIBLE);
            messageSysHolder.tvReject.setVisibility(View.VISIBLE);
            messageSysHolder.tvAccept.setText(R.string.message_sys_type_agree_btn);
            messageSysHolder.tvReject.setText(R.string.message_sys_type_reject_btn);
        }
        messageSysHolder.tvAccept.setOnClickListener(view -> ToastUtil.getInstances().showShort("接受约会邀请"));
        messageSysHolder.tvReject.setOnClickListener(view -> ToastUtil.getInstances().showShort("拒绝约会邀请"));
    }

    @Override
    public int setItemViewType(int position) {
        if (data.size() == 0) {
            return EMPTY_VIEW;
        }
        return ITEM_VIEW;
    }

    @Override
    public int setItemCount() {
        return data == null ? 0 : data.size();
    }

    //系统消息事件监听
    public interface OnMsgClickListener {
        void onItemClick(MessageSystem sysMessage, int position);
    }

    public void updateShowTime() {
        mLastTime = 0;
        for (MessageSystem sysMessage : data) {
            //更新系统消息时间
            //            if (mLastTime == 0 || mLastTime - sysMessage.getCreated() > 0) {//5 * 60
            //                mLastTime = sysMessage.getCreated();
            //                sysMessage.setShowTitleTime(true);
            //            } else {
            //                sysMessage.setShowTitleTime(false);
            //            }

            sysMessage.setShowTitleTime(true);
        }
        notifyDataSetChanged();
    }
}
