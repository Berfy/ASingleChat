package cn.polaris.mqttuikit.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.view.CircleImageView;
import cn.berfy.sdk.mvpbase.view.SwipeItemLayout;
import cn.berfy.service.im.manager.IMManager;
import cn.berfy.service.im.model.Message;
import cn.berfy.service.im.model.MessageType;
import cn.berfy.service.im.model.contact.UserInfo;
import cn.berfy.service.im.model.conversation.Conversation;
import cn.berfy.service.im.model.conversation.NormalConversation;
import cn.berfy.service.im.util.IMTimeUtil;
import cn.polaris.mqttuikit.R;
import cn.polaris.mqttuikit.holder.BaseViewHolder;
import cn.polaris.mqttuikit.iview.IRecyclerView;

/**
 * 消息会话列表的adapter
 *
 * @author NorthStar
 * @date 2019/1/2 15:48
 */
public class ConversationAdapter extends RecyclerView.Adapter<BaseViewHolder> implements IRecyclerView {

    private final static String TAG = "IM_会话列表适配器";
    private Activity mContext;
    private int mResourceId;
    private LayoutInflater mInflater;
    private List<NormalConversation> mConversations = new ArrayList<>();

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     */
    public ConversationAdapter(Activity context, int resource) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResourceId = resource;
    }

    public List<NormalConversation> getData() {
        return mConversations;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(mResourceId, parent, false));
    }

    @Override
    public int getItemCount() {
        return mConversations.size();
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;
            NormalConversation normalConversation = mConversations.get(position);
            updateConversation(viewHolder, normalConversation);
            viewHolder.addOnClickListener(R.id.tv_delete);
        }
    }

    /**
     * 更新会话
     */
    private void updateConversation(ViewHolder holder, NormalConversation normalConversation) {
        Conversation conversation = normalConversation.getConversation();
        boolean isChatRoom = conversation.getType() == MessageType.TYPE_CHATROOM;
        holder.chatRoomView.setVisibility(isChatRoom ? View.VISIBLE : View.INVISIBLE);
        holder.swipeItemLayout.setTag(isChatRoom ? "noDelete" : "canDelete");
        getConversationInfo(holder, conversation);
        if (!isChatRoom) {//聊天室只有会话入口没有消息详情
            Message message = normalConversation.getLastMessage();
            long msgTime = 0;
            String lastMessage = "";
            if (message != null) {
                msgTime = message.getTime();
                lastMessage = message.getSummaryText();
            }
            msgTime = msgTime == 0 ? conversation.getLastMessageTime() / 1000 : msgTime / 1000;
            lastMessage = !TextUtils.isEmpty(lastMessage) ? lastMessage : conversation.getLastMessage();
            holder.lastMessage.setText(lastMessage);

            if (msgTime != 0) {
                holder.time.setVisibility(View.VISIBLE);
                holder.time.setText(IMTimeUtil.getTimeStr(msgTime));
            } else {
                holder.time.setVisibility(View.GONE);
            }
            setUnreadNum(holder, conversation);
            //设置消息免打扰
            boolean isNotNotifyMsg = IMManager.Companion.getInstance().isNotNotifyMsg(conversation.getToId());//是否免打扰
            holder.im_iv_not_notify.setVisibility(isNotNotifyMsg ? View.VISIBLE : View.INVISIBLE);
        } else {
            holder.lastMessage.setVisibility(View.GONE);
            holder.time.setVisibility(View.GONE);
            holder.unread.setVisibility(View.GONE);
        }
    }

    private void getConversationInfo(ViewHolder holder, Conversation conversation) {
        String uid = conversation.getToId();
        if (holder.isRecyclable()) {
            IMManager.Companion.getInstance().getContactsManager().getUserInfo(uid, new RequestCallBack<UserInfo>() {
                @Override
                public void onStart() {

                }

                @Override
                public void onFinish(NetResponse<UserInfo> response) {
                    if (response.isOk()) {
                        String url = response.data.getAvatar();
                        String nickName = response.data.getNickname();
                        nickName = !TextUtils.isEmpty(nickName) ? nickName : conversation.getTitle();
                        conversation.setTitle(nickName);
                        setUserInfo(url, nickName, holder);
                        LogF.d(TAG, "getUserInfo  onFinish1 nickName" + nickName);
                    } else {
                        String nickName = conversation.getTitle();
                        LogF.d(TAG, "getUserInfo onFinish2 nickName" + nickName);
                        holder.mImNameTV.setText(nickName);
                    }

                }

                @Override
                public void onError(NetError error) {
                    String nickName = conversation.getTitle();
                    LogF.d(TAG, "getUserInfo onError nickName" + nickName);
                    holder.mImNameTV.setText(nickName);
                }
            });
        }
    }

    private void setUserInfo(String url, String nickName, ViewHolder viewHolder) {
        viewHolder.mImNameTV.setText(nickName);

        int defaultIcon = R.drawable.placeholder;
        if (mContext != null && !mContext.isFinishing()) {
            Glide.with(mContext)
                    .load(url)
                    .asBitmap()
                    .placeholder(defaultIcon)
                    .error(defaultIcon)
                    .into(viewHolder.avatar);
        }
    }

    private void setUnreadNum(ViewHolder viewHolder, Conversation conversation) {
        long unread = conversation.getUnreadNum();

        if (unread <= 0) {
            viewHolder.unread.setVisibility(View.GONE);
        } else {
            viewHolder.unread.setVisibility(View.VISIBLE);
            /*String unReadStr = String.valueOf(unread);
            viewHolder.unread.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            if (unread < 10) {
            } else {
                if (unread > 99) {
                    unReadStr = CommonUtil.getString(mContext, R.string.im_time_more);
                    viewHolder.unread.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                }
            }
            viewHolder.unread.setText(unReadStr);*/
        }
    }

    //    private void getGroupInfo(ViewHolder viewHolder, Conversation payload) {
    //        viewHolder.swipeItemLayout.setTag("noDelete");
    //        String type = payload.getType();
    //        if (TextUtils.isEmpty(type)) type = MessageType.INSTANCE.getTYPE_P2P();
    //        Message message = payload.getMessage();
    //        if (null == message) {
    //            message = new Message() {
    //                @Override
    //                public void getRemoteUrl(OnMessageDownloadCallback callback) {
    //                }
    //            };
    //            payload.setMessage(message);
    //        }
    //        LogF.d(TAG, "没有群组信息 SDK获取");
    //        updateInfo(type, viewHolder, message);
    //    }

    /*private void setContent(String content, TextView tv, @ColorRes int resColor) {
        if (!TextUtils.isEmpty(content)) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(content);
            GradientDrawable myGrad = (GradientDrawable) tv.getBackground();
            myGrad.setColor(ContextCompat.getColor(mContext, resColor));
        } else {
            tv.setVisibility(View.GONE);
        }
    }*/


    public class ViewHolder extends BaseViewHolder {

        private SwipeItemLayout swipeItemLayout;
        private FrameLayout chatRoomView;
        private CircleImageView avatar;
        private TextView time;
        private TextView unread;
        private TextView mImNameTV;
        private TextView lastMessage;
        private ImageView im_iv_not_notify;

        ViewHolder(View view) {
            super(view);
            swipeItemLayout = view.findViewById(R.id.swipeItemLayout);
            chatRoomView = view.findViewById(R.id.chat_room_view);
            mImNameTV = view.findViewById(R.id.im_tv_name);
            lastMessage = view.findViewById(R.id.im_last_message);
            avatar = view.findViewById(R.id.im_avatar);
            time = view.findViewById(R.id.im_message_time);
            unread = view.findViewById(R.id.im_unread_num);
            im_iv_not_notify = view.findViewById(R.id.im_iv_not_notify);
        }
    }

    @Override
    public int getHeaderLayoutCount() {
        return 0;
    }
}
