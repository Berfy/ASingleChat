package cn.zcgames.sdk.mqttsdk.message.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.prensenter.BasePresenter;
import cn.berfy.sdk.mvpbase.util.AppUtils;
import cn.polaris.mqttuikit.view.activity.ChatSettingActivity;
import cn.polaris.mqttuikit.view.fragment.ChatFragment;
import cn.zcgames.sdk.im.R;
import cn.zcgames.sdk.mqttsdk.personal.view.activity.PersonInfoActivity;

/**
 * IM聊天界面
 *
 * @author NorthStar
 * @date 2018/12/24 18:33
 */
public class ChatActivity extends CommonActivity {

    private static final String TAG = "IM聊天";
    public static final String NICK_KEY = "title";
    public static final String TOID = "toId";
    public static final String MSGTYPE = "msgType";
    private String toId;
    private String title;
    private int conversionType;
    private ChatFragment mChatFragment;

    /**
     *
     * @param context
     * @param conversionType 会话类型 0 单聊 1群聊 2.聊天室
     * @param toId   对方的uid
     * @param title  对方昵称
     */
    public static void launch(Activity context, int conversionType, String toId, String title) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(NICK_KEY, title);
        intent.putExtra(TOID, toId);
        intent.putExtra(MSGTYPE, conversionType);
        context.startActivity(intent);
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        //调用conversation.getMessage()获取数据源,监听这个新消息
        Intent intent = getIntent();
        title = intent.getStringExtra(NICK_KEY);
        toId = intent.getStringExtra(TOID);
        conversionType = intent.getIntExtra(MSGTYPE,0);
    }

    @Override
    public int getContentViewId() {
        return R.layout.im_activity_chat;
    }

    @Override
    public void initView() {
        setTitleBar();
        setFragment();
    }

    @Override
    public BasePresenter initPresenter() {
        return null;
    }

    private void setTitleBar() {
        showTitleBar();
        darkStatusBar(true);
        setFitsSystemWindows(true);
        getTitleBar().showNormalTheme();
        getTitleBar().setTitle(title);
        getTitleBar().showStatusBarMargin(false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (conversionType == 0) {//单聊设置
            getTitleBar().setRightIcon(true, R.drawable.chat_menu, v -> ChatSettingActivity.launch(mContext, toId));
        }
        getTitleBar().setOnBackListener(view -> {
            if (null == mChatFragment) finish();
            else mChatFragment.close();
        });
    }


    private void setFragment() {
        mChatFragment = ChatFragment.newInstance(toId, title, conversionType);
        mChatFragment.setChatClickListener(uid -> {
            if (!AppUtils.checkJump()) return;
            PersonInfoActivity.launch(mContext, uid);
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.im_frame_layout, mChatFragment).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getFragmentManager() != null) {
            getSupportFragmentManager().beginTransaction().remove(mChatFragment).commitAllowingStateLoss();
        }
        mChatFragment = null;
    }
}
