package cn.polaris.mqttuikit.view.fragment;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.berfy.sdk.mvpbase.base.CommonFragment;
import cn.berfy.sdk.mvpbase.manager.ThreadPoolManager;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.AppUtils;
import cn.berfy.sdk.mvpbase.util.CommonUtil;
import cn.berfy.sdk.mvpbase.util.GsonUtil;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.util.NoAlphaItemAnimator;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.berfy.sdk.mvpbase.view.SwipeItemLayout;
import cn.berfy.service.im.manager.CacheManager;
import cn.berfy.service.im.manager.ConversationManager;
import cn.berfy.service.im.manager.IMManager;
import cn.berfy.service.im.manager.i.IMCallback;
import cn.berfy.service.im.manager.i.OnConnectStatusCallback;
import cn.berfy.service.im.manager.i.OnMessageListener;
import cn.berfy.service.im.model.Message;
import cn.berfy.service.im.model.MessageContentType;
import cn.berfy.service.im.model.MessageCustom;
import cn.berfy.service.im.model.MessageCustomType;
import cn.berfy.service.im.model.MessageType;
import cn.berfy.service.im.model.conversation.Conversation;
import cn.berfy.service.im.model.conversation.NormalConversation;
import cn.polaris.mqttuikit.R;
import cn.polaris.mqttuikit.adapter.ConversationAdapter;
import cn.polaris.mqttuikit.iview.SimpleClickListener;
import cn.polaris.mqttuikit.utils.ConversationComparator;
import cn.polaris.mqttuikit.view.activity.MessageSysActivity;

/**
 * 消息页面
 *
 * @author NorthStar
 * @date 2018/12/29 14:28
 */
public class MessageFragment extends CommonFragment implements OnConnectStatusCallback, OnMessageListener {

    private final String TAG = "MessageFragment";
    private ConversationAdapter mAdapter;
    private ConversationManager mConversationManager;
    private MsgMenuClickListener mListener;
    private PopupWindow popup;
    private View rightView;
    private ConversationComparator conversationComparator;//用于给会话排序
    private IMManager mImManager;
    private List<String> toIds = new ArrayList<>();//存放所有已展示会话的toId

    public static MessageFragment newInstance() {
        MessageFragment messageFragment = new MessageFragment();
        Bundle bundle = new Bundle();
        messageFragment.setArguments(bundle);
        return messageFragment;
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_message;
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        LogF.d(TAG, "userId==>" + IMManager.Companion.getInstance().getLoginId());
    }

    //建立mqtT连接
    private void initMqtT() {
        mImManager = IMManager.Companion.getInstance();
        mConversationManager = ConversationManager.Companion.getInstance();
        conversationComparator = new ConversationComparator();
        mImManager.addConnectListener(this);
        mImManager.addMessageListener(this);
    }

    //获取本地会话列表
    public void getLocalConversations() {
        if (mConversationManager != null) {
            mConversationManager.getConversations(new CacheManager.OnDataCallback<Conversation>() {
                @Override
                public void onSuc(@NonNull List<Conversation> data) {
                    int size = data.size();
                    if (size > 0) {
                        toIds.clear();//清空toId的缓存集合
                        List<NormalConversation> conversations = new ArrayList<>();
                        for (Conversation conversation : data) {
                            NormalConversation normalConversation = new NormalConversation(conversation);
                            toIds.add(conversation.getToId());
                            conversations.add(normalConversation);
                        }
                        LogF.d(TAG, "getConversation----conversations----size===>" + size);
                        mAdapter.getData().clear();
                        mAdapter.getData().addAll(conversations);
                        OrderByMsg();
                        mAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailed(@NonNull String err) {
                    LogF.d(TAG, "errMsg==>" + err);
                }
            });
        }
        //更新每条conversion中的最新消息
        //        for (NormalConversation conversation : mAdapter.getData()) {
        //            conversation.getConversation().getMessage("0", 1);
        //        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void updateMessageEvent(Message msg) {
        String senderId = msg.getSenderId();
        String loginId = IMManager.Companion.getInstance().getLoginId();
        boolean isSelf = loginId.equals(senderId);
        if (!isSelf) {
            LogF.d(TAG, "updateMessageEvent==>不是自己发送的不用本地插入集合");
            return;
        }
        Conversation conversation = msg.getConversation();
        if (conversation == null) {
            LogF.d(TAG, "updateMessageEvent==>会话为空");
            return;
        }
        if (conversation.getType() == MessageType.TYPE_CHATROOM) {
            LogF.d(TAG, "updateMessageEvent==>聊天室消息不添加");
            return;
        }
        String lastToId = conversation.getToId();
        boolean hasToId = toIds.contains(lastToId);
        if (!hasToId) {
            toIds.add(lastToId);
            mAdapter.getData().add(new NormalConversation(conversation));
            OrderByMsg();
            mAdapter.notifyDataSetChanged();
        } else {
            for (NormalConversation normalConversation : mAdapter.getData()) {
                String toId = normalConversation.getConversation().getToId();
                if (toId.equals(lastToId)) {
                    normalConversation.setConversation(conversation);
                    break;
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void updateNotNotifyEvent(Conversation lastConversation) {
        if (lastConversation == null) return;
        if (!TextUtils.isEmpty(lastConversation.getToId())) {
            String lastToId = lastConversation.getToId();
            ThreadPoolManager.getInstance().execute(() -> {
                for (NormalConversation normalConversation : mAdapter.getData()) {
                    Conversation conversation = normalConversation.getConversation();
                    String toId = conversation.getToId();
                    if (toId.equals(lastToId)) {
                        conversation.setNotNotify(lastConversation.isNotNotify());
                        normalConversation.setConversation(conversation);
                        break;
                    }
                }
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> mAdapter.notifyDataSetChanged());
            });
        }
    }

    //更新最新消息
    public void updateLastMsg(Message lastMsg) {
        //判断消息类型 是群组还是单聊
        MessageContentType msgType = lastMsg.getType();
        Conversation lastConversation = lastMsg.getConversation();
        if (lastConversation == null) return;
        boolean hasConversation = false;//会话归属设置;
        String lastToId = lastConversation.getToId();
        for (NormalConversation normalConversation : mAdapter.getData()) {
            String toId = normalConversation.getConversation().getToId();
            if (toId.equals(lastToId)) {
                normalConversation.setLastMessage(lastMsg);
                LogF.d(TAG, "这是已有会话更新==>" + lastMsg.getSummaryText() + " ,lastToId==>" + lastToId);
                hasConversation = true;
                break;
            }
        }
        if (!hasConversation) {
            NormalConversation normalConversation = new NormalConversation(lastConversation);
            normalConversation.setLastMessage(lastMsg);
            toIds.add(lastToId);
            mAdapter.getData().add(normalConversation);
            LogF.d(TAG, "这是新的会话==>" + normalConversation.getSummaryText() +
                    " ,lastToId==>" + lastToId + "  lastMsg== > " + GsonUtil.getInstance().toJson(lastMsg));
        }
        OrderByMsg();
        mAdapter.notifyDataSetChanged();
        mListener.updateRedPoint(true);//更新小红点
    }

    //更新小红点
    private void updateUnReadNum() {
        long totalUnreadNum = getTotalUnreadNum();
        if (totalUnreadNum==0)mListener.updateRedPoint(false);
    }

    private long getTotalUnreadNum() {
        long num = 0;
        for (NormalConversation normalConversation : mAdapter.getData()) {
            Conversation conversation = normalConversation.getConversation();
            num += conversation.getUnreadNum();
        }
        LogF.d(TAG, "未读消息" + num);
        return num;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUnReadNum();//更新小红点
    }

    @Override
    public void initView() {
        initMqtT();//初始化mqtT
        initChatRv();//初始化
        setTitleBar();
        EventBus.getDefault().register(this);
        getLocalConversations();
    }

    public void addFriendMsg(Conversation conversation) {
        if (conversation == null || mAdapter == null) return;
        String toId = conversation.getToId();
        toIds.add(toId);
        mAdapter.getData().add(new NormalConversation(conversation));
        OrderByMsg();
        mAdapter.notifyDataSetChanged();
    }

    private void setTitleBar() {
        showTitleBar();
        rightView = getTitleBar().getRightView();
        getTitleBar().setLeftIcon(false);
        getTitleBar().setTitle("聊天");
        getTitleBar().setTitleColor(ContextCompat.getColor(mContext, R.color.white_normal));
        getTitleBar().setRightIcon(true, R.drawable.add, v -> setMenuClickListener());
    }

    // 定义IM_UIKit与APP间的功能交互
    public interface MsgMenuClickListener {
        void addFriend();//添加朋友

        void updateRedPoint(boolean isShow);//更新小红点

        void startChatGroup();//开启群聊

        void launchChatActivity(String toId, String title, int conversionType);//点击item跳转到聊天页面
    }

    public void setMsgMenuClickListener(MsgMenuClickListener listener) {
        mListener = listener; // 获取到宿主activity并给接口赋值
    }

    //定义"+"按钮UI交互
    private void setMenuClickListener() {
        View popView = LayoutInflater.from(mContext).inflate(R.layout.popwindow_msg_menu, null);
        popup = new PopupWindow(popView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popup.setContentView(popView);
        popup.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popup.setFocusable(true);
        popup.setTouchable(true);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(new ColorDrawable(CommonUtil.getColor(R.color.transparent)));
        popView.findViewById(R.id.tv_add_friend).setOnClickListener(v -> {
            mListener.addFriend();
            popup.dismiss();
        });
        popView.findViewById(R.id.tv_group_chat).setOnClickListener(v -> {
            //            mListener.startChatGroup();
            ToastUtil.getInstances().showShort("暂未开放");
            popup.dismiss();
        });

        //设置水平偏移量
        popup.getContentView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWith = popup.getContentView().getMeasuredWidth();
        int xOff = rightView.getWidth() - popupWith - (int) getResources().getDimension(R.dimen.space_8);

        if (popup.isShowing()) {
            popup.dismiss();
        } else {
            popup.showAsDropDown(rightView, xOff, 0, Gravity.BOTTOM);
        }
    }

    private void initChatRv() {
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.msg_rv);
        mRecyclerView.setItemAnimator(new NoAlphaItemAnimator());
        mAdapter = new ConversationAdapter(getActivity(), R.layout.im_item_conversation);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }

            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        });
        mRecyclerView.addOnItemTouchListener(new SwipeItemLayout.OnSwipeItemTouchListener(getActivity()));
        mRecyclerView.addOnItemTouchListener(touchListener);
        findViewById(R.id.ll_system_view).setOnClickListener(v -> MessageSysActivity.launcher(getActivity()));
    }

    private SimpleClickListener<ConversationAdapter> touchListener = new SimpleClickListener<ConversationAdapter>() {
        @Override
        public void onItemClick(ConversationAdapter adapter, View view, int position) {
            if (adapter != null) {
                NormalConversation normalConversation = adapter.getData().get(position);
                Conversation conversation = normalConversation.getConversation();
                if (conversation.getUnreadNum() == 1) {
                    conversation.setUnreadNum(0);
                    adapter.getData().get(position).setConversation(conversation);
                    adapter.notifyItemChanged(position);
                }
                String title = conversation.getTitle();
                String toId = conversation.getToId();
                MessageType msgType = conversation.getType();
                int conversionType = 0;
                switch (msgType) {
                    case TYPE_P2P://单聊
                        conversionType = 0;
                        break;

                    case TYPE_GROUP://群聊
                        conversionType = 1;
                        break;

                    case TYPE_CHATROOM://聊天室
                        conversionType = 2;
                        break;
                }
                mListener.launchChatActivity(toId, title, conversionType);
            }
        }

        @Override
        public void onItemLongClick(ConversationAdapter adapter, View view, int position) {
            //                        showLongClickMenu(adapter.getItem(position), position);
        }

        @Override
        public void onItemChildClick(ConversationAdapter adapter, View view, int position) {
            if (view != null && AppUtils.checkJump()) {
                NormalConversation normalConversation = adapter.getData().get(position);
                Conversation conversation = normalConversation.getConversation();
                //删除会话消息
                String toId = conversation.getToId();
                MessageType type = conversation.getType();
                if (!TextUtils.isEmpty(toId)) {
                    mConversationManager.deleteConversation(toId, type, new IMCallback() {
                        @Override
                        public void onSuc() {
                            LogF.d(TAG, "会话删除成功");
                        }

                        @Override
                        public void onFailed(@NonNull String err) {
                            LogF.d(TAG, "会话删除失败" + err);
                        }
                    });
                }
                toIds.remove(toId);
                mAdapter.getData().remove(normalConversation);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onItemChildLongClick(ConversationAdapter adapter, View view, int position) {

        }
    };

    @Override
    public BasePresenter initPresenter() {
        return null;
    }

    @Override
    public void connectStart() {
        LogF.d(TAG, "正在连接");
    }

    @Override
    public void connectSuc() {
        LogF.d(TAG, "连接成功");
    }

    @Override
    public void connectFailed(@Nullable Throwable exception) {
        LogF.d(TAG, "连接失败");
        if (null != exception) LogF.d(TAG, "失败原因==>" + exception.getMessage());
    }

    @Override
    public void disConnect(@Nullable Throwable exception) {
        LogF.d(TAG, "断开连接");
        if (null != exception) LogF.d(TAG, "断连原因==>" + exception.getMessage());
    }

    @Override//P2P消息
    public void newMessage(@Nullable Message message) {
        if (message == null) {
            LogF.d(TAG, "消息为空 作废");
            return;
        }
        Conversation conversation = message.getConversation();
        if (conversation == null) {
            LogF.d(TAG, "消息会话为空 作废");
            return;
        }
        if (conversation.getType() == MessageType.TYPE_CHATROOM) {
            LogF.d(TAG, "updateMessageEvent==>聊天室消息不添加");
            return;
        }
        LogF.d(TAG, "MessageFragment--收到新消息  newMessage   sender=" + message.getSenderId());
        updateLastMsg(message);
    }

    @Override
    public void systemMessage(@Nullable MessageCustom message) {
        if (message == null) return;
        LogF.d(TAG, "收到系统消息  newMessage   " + GsonUtil.getInstance().toJson(message));
        if (message.getCus_type() == MessageCustomType.TYPE_AGREE_ADD_FRIEND) {
            getLocalConversations();
        }
    }

    @Override
    public void refreshConversation() {
        getLocalConversations();
        updateUnReadNum();
    }

    @Override
    public void sendMessageStatus(@Nullable String message, boolean isSuc) {
        LogF.d(TAG, isSuc ? "消息发送成功" : "消息发送失败" + message);
    }

    //给会话排序
    private void OrderByMsg() {
        if (mAdapter != null) {
            int size = mAdapter.getData().size();
            if (size > 0) {
                Collections.sort(mAdapter.getData(), conversationComparator);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mImManager) {
            mImManager.removeMessageListener(this);
        }
        EventBus.getDefault().unregister(this);
    }
}
