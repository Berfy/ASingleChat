package cn.polaris.mqttuikit.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.ToggleButton;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.config.CacheConstant;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.LogF;
import cn.berfy.sdk.mvpbase.util.SharedPreferenceUtil;
import cn.berfy.service.im.manager.IMManager;
import cn.berfy.service.im.model.MessageType;
import cn.berfy.service.im.model.conversation.Conversation;
import cn.berfy.service.im.model.conversation.NotifyToggleInfo;
import cn.polaris.mqttuikit.R;

/**
 * 设置聊天界面功能配置
 *
 * @author NorthStar
 * @date 2019/2/11 12:15
 */
public class ChatSettingActivity extends CommonActivity {
    public static final String TAG = "IM_免打扰设置";
    public static final String TOID = "toId";
    private String toId;
    private boolean isOpen = false;//是否对当前会话开启了消息免打扰
    private ToggleButton notNotifyBtn;
    private NotifyToggleInfo notifyToggleInfo;//配置信息
    private Map<String, NotifyToggleInfo> mTogglesMap; //保存通知开关配置
    private String userId;//作为保存会话列表中消息免打扰开关配置的KEY
    private boolean isChecked;//最终免打扰开关的状态

    @Override
    public int getContentViewId() {
        return R.layout.activity_chat_setting;
    }

    public static void launch(Activity context, String toId) {
        Intent intent = new Intent(context, ChatSettingActivity.class);
        intent.putExtra(TOID, toId);
        context.startActivity(intent);
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        toId = getIntent().getStringExtra(TOID);
        IMManager imManager = IMManager.Companion.getInstance();
        userId = imManager.getLoginId();
        LogF.d(TAG, "userId==>" + userId + " ,toId==>" + toId);
        if (!TextUtils.isEmpty(userId)) {
            mTogglesMap = SharedPreferenceUtil.getHashMapData(
                    this, CacheConstant.XML_NOT_NOTIFY_DATA, NotifyToggleInfo.class);
            if (mTogglesMap != null) notifyToggleInfo = mTogglesMap.get(userId);
            if (notifyToggleInfo != null) {
                List<String> notNotifyIds = notifyToggleInfo.getNotNotifyIds();
                if (notNotifyIds != null && notNotifyIds.size() > 0) {
                    isOpen = notNotifyIds.contains(toId);
                }
            } else {
                notifyToggleInfo = new NotifyToggleInfo();
            }
        }
    }

    @Override
    public void initView() {
        setTitleBar();
        init();
        setListener();
    }

    private void init() {
        notNotifyBtn = findViewById(R.id.toggle_not_notify);
        notNotifyBtn.setChecked(isOpen);
    }

    private void setListener() {
        notNotifyBtn.setOnClickListener(v -> {//设置消息免打扰缓存数据
            if (!TextUtils.isEmpty(userId)) {
                isChecked = ((ToggleButton) v).isChecked();
                LogF.d(TAG, "isChecked==>" + isChecked);
                if (notifyToggleInfo == null) notifyToggleInfo = new NotifyToggleInfo();
                List<String> notNotifyIds = notifyToggleInfo.getNotNotifyIds();
                if (isChecked) {
                    if (!notNotifyIds.contains(toId)) {
                        notNotifyIds.add(toId);
                    }
                } else {
                    notNotifyIds.remove(toId);
                }
                notifyToggleInfo.setNotNotifyIds(notNotifyIds);
                if (mTogglesMap == null) mTogglesMap = new LinkedHashMap<>();
                mTogglesMap.put(userId, notifyToggleInfo);//订阅或注销对应话题
                SharedPreferenceUtil.putHashMapData(ChatSettingActivity.this, CacheConstant.XML_NOT_NOTIFY_DATA, mTogglesMap);
                LogF.d(TAG, "免打扰, toId==>" + toId + " ,notNotifyIds.size()==>" + notNotifyIds.size());
            }
        });
    }

    private void setTitleBar() {
        showTitleBar();
        darkStatusBar(false);
        getTitleBar().setTitle("聊天信息");
        getTitleBar().showStatusBarMargin(true);
        getTitleBar().setOnBackListener(view -> close());
    }

    @Override//双击退出app事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            close();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void close() {
        boolean isChange = isChecked != isOpen;
        if (isChange) {
            Conversation conversation = new Conversation(toId, MessageType.TYPE_P2P);
            conversation.setNotNotify(isChecked);
            EventBus.getDefault().postSticky(conversation);
        }
        finish();
    }

    @Override
    public BasePresenter initPresenter() {
        return null;
    }
}
