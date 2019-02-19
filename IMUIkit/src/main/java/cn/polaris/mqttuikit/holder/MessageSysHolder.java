package cn.polaris.mqttuikit.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.polaris.mqttuikit.R;


/**
 * 官方消息viewHolder
 * @author NorthStar
 * @date  2019/1/3 15:58
 */
public class MessageSysHolder extends RecyclerView.ViewHolder {

    public TextView tvNotifyTitle;
    public ImageView imvHead;
    public TextView mTvShowTime;
    public TextView tvMsgContent;
    public TextView tvReject;
    public TextView tvAccept;
    public View space;
    public LinearLayout msgLayout;
    public RelativeLayout bottomLayout;

    public MessageSysHolder(View itemView) {
        super(itemView);
        mTvShowTime = itemView.findViewById(R.id.tv_show_time);
        tvNotifyTitle = itemView.findViewById(R.id.tv_notify_title);
        imvHead = itemView.findViewById(R.id.im_hint_icon);
        tvMsgContent = itemView.findViewById(R.id.tv_msg_content);
        tvReject = itemView.findViewById(R.id.tv_reject);
        tvAccept = itemView.findViewById(R.id.tv_accept);
        space = itemView.findViewById(R.id.msg_space_view);
        msgLayout = itemView.findViewById(R.id.layout_msg_content);
        bottomLayout = itemView.findViewById(R.id.layout_bottom);
    }
}
