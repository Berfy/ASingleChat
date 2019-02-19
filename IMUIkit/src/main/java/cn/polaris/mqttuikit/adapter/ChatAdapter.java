package cn.polaris.mqttuikit.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.List;

import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.service.im.manager.IMManager;
import cn.berfy.service.im.model.ChatViewHolder;
import cn.berfy.service.im.model.Message;
import cn.berfy.service.im.model.MessageType;
import cn.berfy.service.im.model.contact.UserInfo;
import cn.berfy.service.im.model.conversation.Conversation;
import cn.berfy.service.im.util.IMTimeUtil;
import cn.polaris.mqttuikit.R;
import cn.polaris.mqttuikit.iview.OnMessageEventListener;

/**
 * 聊天界面adapter
 *
 * @author NorthStar
 * @date 2018/12/25 17:09
 */
public class ChatAdapter extends ArrayAdapter<Message> {

    private final String TAG = "IM_ChatAdapter";
    private Activity mContext;
    private LayoutInflater inflater;
    private List<Message> mMessage;
    private int mResourceId;
    private OnMessageEventListener mOnMessageEventListener;
    private boolean isSelf = false;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param messages The objects to represent in the ListView.
     */
    public ChatAdapter(Activity context, int resource, List<Message> messages) {
        super(context, resource, messages);
        mContext = context;
        mMessage = messages;
        mResourceId = resource;
        inflater = LayoutInflater.from(mContext);
    }

    public void setOnMessageEventListener(OnMessageEventListener onMessageEventListener) {
        mOnMessageEventListener = onMessageEventListener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ChatViewHolder viewHolder;
        if (convertView != null) {
            viewHolder = (ChatViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(mResourceId, null);
            viewHolder = new ChatViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        if (position < getCount()) {
            Message msg = getItem(position);
            viewHolder.setPosition(position);
            if (msg != null) {
                msg.showMessage(viewHolder, mContext);
                viewHolder.setMessages(mMessage);
                isSelf = msg.isSelf();
                updateInfo(msg, viewHolder);//更新UI
                //消息发送错误重发
                viewHolder.getError().setOnClickListener(view -> {
                    if (null != mOnMessageEventListener) {
                        mOnMessageEventListener.reSendMessage(position);
                    }
                });

                viewHolder.getIvReceiver().setOnClickListener(v -> mOnMessageEventListener.avatarClick(msg));
            }
        }
        return convertView;
    }

    //每一条消息都是发送方发送来的,开发者只需要知道发送方的资料显示即可
    private void updateInfo(Message message, ChatViewHolder viewHolder) {
        long msgTime = message.getTime() / 1000;
        if (msgTime != 0) {
            viewHolder.getTimeMessage().setVisibility(View.VISIBLE);
            viewHolder.getTimeMessage().setText(IMTimeUtil.getTimeStr(msgTime));
        }
        Conversation conversation = message.getConversation();
        String uid = "";
        if (conversation != null) {
            MessageType type = conversation.getType();
            if (type == MessageType.TYPE_GROUP) {
                viewHolder.getLeftSender().setVisibility(View.VISIBLE);
                viewHolder.getRightSender().setVisibility(View.VISIBLE);
            } else {
                viewHolder.getLeftSender().setVisibility(View.GONE);
                viewHolder.getRightSender().setVisibility(View.GONE);
            }
            if (type != MessageType.TYPE_P2P) {
                uid = message.getSenderId();
                LogF.d(TAG, "chatAdapter-->getSenderId===>" + uid);
            } else {
                uid = isSelf ? IMManager.Companion.getInstance().getLoginId() : conversation.getToId();
            }
            if (!TextUtils.isEmpty(uid)) {
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

                            getUserInfo(url, nickName, type, viewHolder);
                        } else {
                            getUserInfo("", conversation.getTitle(), type, viewHolder);
                        }

                    }

                    @Override
                    public void onError(NetError error) {
                        getUserInfo("", conversation.getTitle(), type, viewHolder);
                    }
                });
            }
        }
    }

    private void getUserInfo(String url, String nickName, MessageType type, ChatViewHolder viewHolder) {
        boolean isGroup = type == MessageType.TYPE_GROUP || type == MessageType.TYPE_CHATROOM;
        if (isSelf) {
            viewHolder.getRightSender().setText(isGroup ? nickName : "");
            setAvatar(url, viewHolder.getIvSender());
        } else {
            viewHolder.getLeftSender().setText(isGroup ? nickName : "");
            setAvatar(url, viewHolder.getIvReceiver());
        }
    }

    //设置发送方的头像
    private void setAvatar(String url, ImageView imageView) {
        LogF.d(TAG, "senderUrl==>" + url);
        if (!mContext.isFinishing() && !mContext.isDestroyed()) {
            Glide.with(mContext)
                    .load(url)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(new SimpleTarget<GlideDrawable>() { // 可以解决圆形图片第一次加载不出的问题
                        @Override
                        public void onResourceReady(GlideDrawable resource,
                                                    GlideAnimation<? super GlideDrawable> glideAnimation) {
                            imageView.setImageDrawable(resource); //显示图片
                        }
                    });
        }
    }
}
