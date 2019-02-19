package cn.polaris.mqttuikit.view.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.berfy.sdk.http.callback.RequestCallBack;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.base.CommonFragment;
import cn.berfy.sdk.mvpbase.pictureselector.entities.ImageEntity;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSConfigUtil;
import cn.berfy.sdk.mvpbase.pictureselector.utils.PSConstanceUtil;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.AppUtils;
import cn.berfy.sdk.mvpbase.util.FileUtils;
import cn.berfy.sdk.mvpbase.util.GsonUtil;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.util.SoftKeyboardStateWatcher;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import cn.berfy.sdk.mvpbase.view.PopupList;
import cn.berfy.service.im.manager.IMManager;
import cn.berfy.service.im.manager.i.OnMessageListener;
import cn.berfy.service.im.model.Message;
import cn.berfy.service.im.model.MessageCustom;
import cn.berfy.service.im.model.MessageFile;
import cn.berfy.service.im.model.MessageImage;
import cn.berfy.service.im.model.MessageText;
import cn.berfy.service.im.model.MessageType;
import cn.berfy.service.im.model.MessageVoice;
import cn.berfy.service.im.model.conversation.Conversation;
import cn.polaris.mqttuikit.R;
import cn.polaris.mqttuikit.adapter.ChatAdapter;
import cn.polaris.mqttuikit.iview.IChatView;
import cn.polaris.mqttuikit.iview.OnMessageEventListener;
import cn.polaris.mqttuikit.presenter.ChatPresenter;
import cn.polaris.mqttuikit.utils.MessageComparator;
import cn.polaris.mqttuikit.view.ChatInput;

/**
 * 聊天页面
 */
public class ChatFragment extends CommonFragment implements IChatView, OnMessageEventListener, OnMessageListener {

    private static final String TAG = "ChatFragment";
    public static final String NICK_KEY = "title";
    public static final String TOID = "toId";
    public static final String MSG_TYPE = "msgType";
    private static final int FILE_CODE = 300;
    private static final int PIC_CODE = 1;

    private String title;
    private String toId;
    private int conversionType = -1; //会话类型 0 单聊 1群聊 3.聊天室
    private MessageType msgType;
    private ListView mListView;
    private ChatInput mChatInput;
    private ChatAdapter mAdapter;
    private ChatPresenter mPresenter;
    private MessageComparator msgIdComparator;//用于给消息排序
    private List<Message> mMessageList = new ArrayList<>();
    private Handler mHandler = new Handler();
    private List<String> popupMenuItemList = new ArrayList<>();
    private String mProgressStr;//当前上传进度
    private String msgId = "0";
    private Conversation conversation;
    private IMManager mImManager;
    private String loginId;

    @Override
    public int getContentViewId() {
        return R.layout.fragment_chat;
    }

    public static ChatFragment newInstance(String toId, String title, int conversionType) {
        ChatFragment chatFragment = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TOID, toId);
        bundle.putString(NICK_KEY, title);
        bundle.putInt(MSG_TYPE, conversionType);
        chatFragment.setArguments(bundle);
        return chatFragment;
    }

    private ChatInteraction mListener;

    public interface ChatInteraction {
        //点击头像跳转他人主页
        void launchPersonal(String uid);
    }

    public void setChatClickListener(ChatInteraction listener) {
        mListener = listener;
    }

    @Override
    public void initData(Bundle savedInstanceState) {
    }

    @Override
    public void initView() {
        initData();
        initMqtT();
        init();
        setSoftKeyboard();
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            title = bundle.getString(NICK_KEY);
            toId = bundle.getString(TOID);
            LogF.d(TAG, "initData()-- toId==>" + toId);
            conversionType = bundle.getInt(MSG_TYPE);
            if (conversionType != -1) {
                switch (conversionType) {
                    case 0:
                        msgType = MessageType.TYPE_P2P;
                        break;
                    case 1:
                        msgType = MessageType.TYPE_GROUP;
                        break;
                    case 2:
                        msgType = MessageType.TYPE_CHATROOM;
                        break;
                }
            }
        }
    }

    @Override
    public BasePresenter initPresenter() {
        return null;
    }

    //建立mqtT连接监听并获取新消息
    private void initMqtT() {
        mImManager = IMManager.Companion.getInstance();
        loginId = mImManager.getLoginId();
        LogF.d(TAG, "加入消息监听, loginId==>" + loginId);
        mImManager.addMessageListener(this);
        msgIdComparator = new MessageComparator();
        LogF.d(TAG, "加入消息监听");
    }

    //拉取消息
    public void initConversation() {
        if (TextUtils.isEmpty(toId)) {
            LogF.d(TAG, "没有toId");
            return;
        }
        conversation = new Conversation(toId, msgType);
        //开启会话
        mImManager.startChat(conversation);
        conversation.markReadAll();
    }

    private void getHistoryMsg() {
        //获取历史消息
        if (msgType != MessageType.TYPE_CHATROOM) {
            LogF.d(TAG, "聊天室无历史会话");
        }
        if (conversation == null) {
            initConversation();
        }
        if (TextUtils.isEmpty(loginId)) {
            LogF.d(TAG, "没有loginId");
            return;
        }
        conversation.getMessage(loginId, toId, msgId, 20, new RequestCallBack<ArrayList<Message>>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinish(NetResponse<ArrayList<Message>> response) {
                if (response.isOk() && null != response.data) {
                    LogF.d(TAG, "历史消息" + GsonUtil.getInstance().toJson(response.data));
                    if (mAdapter != null) {
                        mMessageList.addAll(response.data);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onError(NetError error) {
                LogF.d(TAG, "errorMsg==>" + error.errMsg);
            }
        });
    }

    public void init() {
        mPresenter = new ChatPresenter(mContext, this);
        mChatInput = (ChatInput) findViewById(R.id.im_input_panel);
        mChatInput.setChatData(2, this);
        mAdapter = new ChatAdapter(getActivity(), R.layout.im_item_message, mMessageList);
        mAdapter.setOnMessageEventListener(this);
        mListView = (ListView) findViewById(R.id.im_list);
        mListView.setAdapter(mAdapter);
        popupMenuItemList.add(mContext.getString(R.string.im_chat_copy));
        popupMenuItemList.add(mContext.getString(R.string.im_chat_del));
        setListener();
    }

    private void setSoftKeyboard() {
        View mRootView = findViewById(R.id.chat_room_view);
        SoftKeyboardStateWatcher mSoftKeyboardStateWatcher = new SoftKeyboardStateWatcher(mRootView, mContext);
        mSoftKeyboardStateWatcher.addSoftKeyboardStateListener(new SoftKeyboardStateWatcher.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                //列表本来在最底部，所以弹起键盘需要保持底部
                scrollToBottom(-1);
            }

            @Override
            public void onSoftKeyboardClosed() {

            }
        });
    }

    @Override//检查mqtT连接状态
    public void checkMqTtState() {
        if (!TextUtils.isEmpty(loginId) && null != mImManager) {
            LogF.d(TAG, "IM连接状态 isConnectting =>" + mImManager.isConnectting()
                    + "isConnected=>" + mImManager.isConnected() + " isManualClose=>"
                    + mImManager.isManualClose());
            if (!mImManager.isConnectting() && !mImManager.isConnected()) {
                mImManager.setUserInfo(loginId, "");
                mImManager.startConnect();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListener() {
        setOnLongClick(mListView);
        mListView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mChatInput.setInputMode(ChatInput.InputMode.NONE);
                    break;
            }
            return false;
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int firstItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && firstItem == 0) {
                    View firstVisibleItemView = mListView.getChildAt(0);
                    if (firstVisibleItemView != null && firstVisibleItemView.getTop() == 0) {
                        //如果拉到顶端读取更多消息
                        int size = mMessageList.size();
                        Message lastMessage = size > 0 ? mMessageList.get(0) : null;
                        if (lastMessage != null) {
                            msgId = lastMessage.getId();
                            getHistoryMsg();//获取更多聊天信息
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                firstItem = firstVisibleItem;
            }
        });
        mPresenter.start();
    }

    private void setOnLongClick(ListView msgRv) {
        PopupList popupList = new PopupList(mContext);
        popupList.bind(msgRv, popupMenuItemList, new PopupList.PopupListListener() {
            @Override
            public boolean showPopupList(View adapterView, View contextView, int contextPosition) {
                return true;
            }

            @Override
            public void onPopupListClick(View contextView, int contextPosition, int position) {
                Message message = mMessageList.get(contextPosition);
                switch (position) {
                    case 0://复制
                        String msgContent = getMsgContent(message);
                        //获取剪贴板管理器：
                        ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                        // 创建普通字符型ClipData并将ClipData内容放到系统剪贴板里。
                        if (cm != null) {
                            cm.setPrimaryClip(ClipData.newPlainText("text", msgContent));
                            ToastUtil.getInstances().showLong(R.string.copy_success);
                        }

                        break;

                    case 1://删除
                        mMessageList.remove(contextPosition);
                        mAdapter.notifyDataSetChanged();
                        break;
                }
            }
        });
    }

    @Override
    public void onSendStart(Message message) {
        updateMessage(message);
    }

    @Override//失败消息重发
    public void reSendMessage(int position) {
        if (!AppUtils.checkJump()) return;
        Message reMsg = mMessageList.get(position);
        mPresenter.sendMessage(toId, reMsg, msgType);
        mAdapter.remove(reMsg);
        mAdapter.notifyDataSetChanged();
    }


    private void updateMessage(Message message) {
        if (message == null) return;
        if (mMessageList.size() == 0) {
            message.setHasTime(null);
        } else {
            message.setHasTime(mMessageList.get(mMessageList.size() - 1));
        }
        mMessageList.add(message);
        scrollToBottom(mAdapter.getCount() - 1);
        LogF.d(TAG, "开始发送的===>message.getId()--" + message.getId() + " size=" + mMessageList.size());
        //        OrderByMsg();
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onSendMessageFail(int code, String desc, Message message) {
        Toast.makeText(mContext, "消息发送失败", Toast.LENGTH_SHORT).show();
        //        updateMessage(message);
        //        //1.TODO 群已解散
        //        //2.单聊消息发送失败
        //        String id = message.getId();
        //        for (int i = 0; i < mMessageList.size(); i++) {
        //            Message msg = mMessageList.get(i);
        //            if (msg.getId().equals(id)) {
        //                mMessageList.set(i, message);
        //                switch (code) {
        //                    case 80001:
        //                发送内容包含敏感词
        //                        msg.setDesc(getString(R.string.im_chat_content_bad));
        //                        mAdapter.notifyDataSetChanged();
        //                        break;
        //                }
        //            }
        //        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 发送消息成功
     *
     * @param message 返回的消息
     */
    @Override
    public void onSendMessageSuccess(Message message) {
        Toast.makeText(mContext, "消息发送成功", Toast.LENGTH_SHORT).show();
        String id = message.getId();
        //发布粘性事件,针对自己发的消息,且会话列表没有的
        EventBus.getDefault().postSticky(message);
        for (int i = 0; i < mMessageList.size(); i++) {
            Message msg = mMessageList.get(i);
            if (msg.getId().equals(id)) {
                mMessageList.set(i, message);
            }
        }
        //        OrderByMsg();
        LogF.d(TAG, "发送成功的===>message.getId()---" + id + " size=" + mMessageList.size());
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 发送文本消息
     */
    @Override
    public void sendText() {
        String sendText = mChatInput.getSendText();//获取发送的文本消息
        if (!TextUtils.isEmpty(sendText)) {
            LogF.d(TAG, "sendText--msg==>" + sendText);
            MessageText textMsg = new MessageText();
            textMsg.setContent(sendText);
            mPresenter.sendMessage(toId, textMsg, msgType);
        }
        mChatInput.setText("");
    }

    /**
     * 发送图片消息
     */
    @Override
    public void sendPhoto() {
        PSConfigUtil.getInstance()
                .setMaxCount(1)
                .setRightTitle(R.string.im_send)
                .showSelector(this, 1);
    }

    private void sendPhoto(String path) {
        LogF.d(TAG, "选择图片的path==>" + path);
        File file = new File(path);
        long FileLength = file.length();
        if (checkFileLength(FileLength, 10)) {
            //发送图片文件
            LogF.d(TAG, "imgMsg--path==>" + path);
            MessageImage imgMsg = new MessageImage();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int reqWidth;
            int reqHeight;
            int width = options.outWidth;
            int height = options.outHeight;
            if (width > height) {
                reqWidth = 198;
                reqHeight = reqWidth * height / width;
            } else {
                reqHeight = 198;
                reqWidth = width * reqHeight / height;
            }
            LogF.d(TAG, "reqWidth==>" + reqWidth + " ,reqHeight==>" + reqHeight);
            imgMsg.setW(reqWidth);
            imgMsg.setH(reqHeight);
            imgMsg.setLocalPath(path);
            imgMsg.setFileLength(FileUtils.getFileSize(path));
            mPresenter.sendMessage(toId, imgMsg, msgType);
        }
    }

    /**
     * 发送语音消息
     */
    @Override
    public void sendVoice(long duration, String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            LogF.d(TAG, "语音时长为==>" + duration + " ,语音路径为==>" + filePath);
            MessageVoice messageVoice = new MessageVoice();
            messageVoice.setLocalPath(filePath);
            messageVoice.setDuration(duration);
            messageVoice.setFileLength(FileUtils.getFileSize(filePath));
            mPresenter.sendMessage(toId, messageVoice, msgType);
        } else {
            ToastUtil.getInstances().showShort(getString(R.string.im_chat_file_not_exist));
        }
    }

    public boolean checkFileLength(long FileLength, int maxSize) {
        if (FileLength == 0) {
            ToastUtil.getInstances().showShort(getString(R.string.im_chat_file_not_exist));
            return false;
        } else if (FileLength > 1024 * 1024 * maxSize) {
            ToastUtil.getInstances().showShort(getString(R.string.im_chat_file_too_large));
            return false;
        } else {
            return true;
        }
    }

    /**
     * 发送文件
     */
    @Override
    public void sendFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, FILE_CODE);
    }

    private void sendFile(String path) {
        if (path == null) return;
        File file = new File(path);
        long fileLength = file.length();
        if (checkFileLength(fileLength, 10)) {
            MessageFile fileMsg = new MessageFile();
            fileMsg.setLocalPath(path);
            fileMsg.setName(file.getName());
            fileMsg.setFileLength(FileUtils.getFileSize(path));
            mPresenter.sendMessage(toId, fileMsg, msgType);
        }
    }

    /**
     * 发送小视频消息
     */
    @Override
    public void sendVideo(String fileName) {
    }


    /**
     * 视频按钮的点击事件
     */
    @Override
    public void videoAction() {
    }

    @Override
    public void avatarClick(Message msg) {
        //点击头像跳转到他人主页
        Conversation conversation = msg.getConversation();
        if (conversation == null) return;
        if (conversation.getType() == MessageType.TYPE_P2P) {
            String toId = conversation.getToId();
            if (!TextUtils.isEmpty(toId)) mListener.launchPersonal(toId);
        }
    }

    private void scrollToBottom(int position) {
        LogF.d(TAG, "滑动到底部" + position + "  列表数量" + mAdapter.getCount());
        if (position != -1) {
            mListView.setSelection(position);
        }
        mHandler.postDelayed(() -> {
            if (position == -1) {
                mListView.setSelection(mAdapter.getCount() - 1);
            } else {
                mListView.setSelection(position);
            }
        }, 500);
    }


    //获取指定消息内容
    private String getMsgContent(Message message) {
        String msgContent = message.getSummaryText();
        LogF.d(TAG, "要复制的消息内容--msgContent==>" + msgContent);
        return msgContent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case FILE_CODE:
                    sendFile(FileUtils.getFilePath(mContext, data.getData()));
                    break;
                case PIC_CODE:
                    ArrayList<ImageEntity> imgList = data.getParcelableArrayListExtra(PSConstanceUtil.PASS_SELECTED);
                    if (imgList == null) return;
                    if (imgList.size() > 0) {
                        for (ImageEntity imageEntity : imgList) {
                            String path = imageEntity.getPath();
                            if (!TextUtils.isEmpty(path)) sendPhoto(path);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void uploadProgress(float pro, boolean isDone) {
        getPro(pro);
        LogF.d(TAG, "ChatActivity 回调的文件上传进度==>" + mProgressStr + " ,是否完成上传==>" + isDone);
    }

    //获取文件的上传及下载进度
    private void getPro(float pro) {
        float progressF = pro * 100;
        int progressI = (int) Math.floor(progressF);
        LogF.d(TAG, "progressI==>" + progressI);
        mProgressStr = String.valueOf(progressI);
    }

    @Override
    public void onPause() {
        super.onPause();
        //退出聊天界面时输入框有内容，保存草稿
        Editable text = mChatInput.getText();
        boolean hasText = text.length() > 0;
        mPresenter.saveDraft(hasText ? text.toString() : null);
        mPresenter.readMessages();
    }

    @Override
    public void onInputModeChanged(ChatInput.InputMode inputMode) {
    }

    public void close() {
        if (mChatInput.isShowIme()) {//收回键盘
            mChatInput.setInputMode(ChatInput.InputMode.NONE);
        }
        ((Activity) mContext).finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.stop();
        //停止会话
        mImManager.stopChat();
    }

    //给会话排序
    private void OrderByMsg() {
        if (mMessageList != null && mMessageList.size() > 0) {
            Collections.sort(mMessageList, msgIdComparator);
        }
    }

    /**
     * 接收并显示消息
     *
     * @param lastMessage 最新消息
     */
    @Override
    public void newMessage(Message lastMessage) {
        Log.d(TAG, "单聊数据" + (null == lastMessage ? 0 : mMessageList.size()));
        if (lastMessage != null) {
            Conversation conversation = lastMessage.getConversation();
            if (conversation == null) {
                LogF.d(TAG, "会话为空");
                return;
            }
            if (IMManager.Companion.getInstance().getLoginId().equals(lastMessage.getSenderId())) {
                LogF.d(TAG, "自己的消息 作废");
                return;
            }
            boolean sameConversion = toId.equals(conversation.getToId());
            if (!sameConversion) {
                LogF.d(TAG, "不是同个会话");
                return;
            }
            LogF.d(TAG, "newMessage  会话类型==>" + conversation.getType());

            for (Message message : mMessageList) {
                if (message.getTime() == lastMessage.getTime()) {
                    LogF.d(TAG, "消息已读");
                    return;
                }
            }

            if (mMessageList != null && mMessageList.size() > 0) {
                lastMessage.setHasTime(mMessageList.get(mMessageList.size() - 1));
            } else {
                lastMessage.setHasTime(lastMessage);
            }
            mMessageList.add(lastMessage);
            //            OrderByMsg();
            scrollToBottom(mAdapter.getCount() - 1);
            mAdapter.notifyDataSetChanged();
            //当前聊天界面已读上报，用于多终端登录时未读消息数同步
            mPresenter.readMessages();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMessageList.size() == 0) getHistoryMsg();
    }

    @Override
    public void systemMessage(MessageCustom message) {

    }

    @Override
    public void refreshConversation() {
        if (mMessageList.size() == 0) getHistoryMsg();
    }

    @Override
    public void sendMessageStatus(String message, boolean isSuc) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mImManager) {
            mImManager.removeMessageListener(this);
        }
    }
}
